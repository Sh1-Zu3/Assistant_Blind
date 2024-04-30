# main_crawler.py
import argparse
from bs4 import BeautifulSoup, NavigableString
import requests
import tqdm

class Article:
    def __init__(self, title, description, paragraphs):
        self.title = title
        self.description = description
        self.paragraphs = paragraphs

def get_text_from_tag(tag):
    if isinstance(tag, NavigableString):
        return tag
    return tag.text

def extract_content(url):
    content = requests.get(url).content
    soup = BeautifulSoup(content, "html.parser")

    title = soup.find("h1", class_="title-detail")
    if title is None:
        return None, None, None
    title = title.text

    description = [get_text_from_tag(p) for p in soup.find("p", class_="description").contents]
    paragraphs = [get_text_from_tag(p) for p in soup.find_all("p", class_="Normal")]

    return title, description, paragraphs

def write_content(url, articles):
    title, description, paragraphs = extract_content(url)

    if title is not None:
        article = Article(title, description, paragraphs)
        articles.append(article)

def crawl_type(article_type, total_pages):
    print(f"Crawl articles type {article_type}")
    articles = []

    for i in tqdm.tqdm(range(1, total_pages + 1)):
        content = requests.get(f"https://vnexpress.net/{article_type}-p{i}").content
        soup = BeautifulSoup(content, "html.parser")
        titles = soup.find_all(class_="title-news")

        if len(titles) == 0:
            continue

        for title in titles:
            link = title.find_all("a")[0]
            url = link.get("href")
            title, description, paragraphs = extract_content(url)
            if title is not None:
                article = Article(title, description, paragraphs)
                articles.append(article)

    return articles


def crawl_all_types(total_pages):
    all_articles = []

    # Thêm các loại bài báo mà bạn muốn crawl vào danh sách dưới đây
    article_types = ["the-thao", "giai-tri","thoi-su","khoa-hoc"]

    for article_type in article_types:
        articles = crawl_type(article_type, total_pages)
        all_articles.extend(articles)

    return all_articles

def main(article_type, all_types=False, total_pages=1):
    error_urls = list()

    if all_types:
        all_articles = crawl_all_types(total_pages)
        print("All Articles:")
        for article in all_articles:
            print(f"Title: {article.title}")
            print(f"Description: {article.description}")
            print(f"Paragraphs: {article.paragraphs}")
            print("\n" + "=" * 50 + "\n")
        return all_articles
    else:
        articles = crawl_type(article_type, total_pages)
        print("Articles:")
        for article in articles:
            print(f"Title: {article.title}")
            print(f"Description: {article.description}")
            print(f"Paragraphs: {article.paragraphs}")
            print("\n" + "=" * 50 + "\n")
        return articles
    print("Error URLs:")
    for url in error_urls:
        print(url)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="VNExpress urls crawler")

    parser.add_argument("--type", default="du-lich", help="name of articles type", dest="article_type")
    parser.add_argument("--all", default=False, action="store_true", help="crawl all of types", dest="all_types")
    parser.add_argument("--pages", default=1, type=int, help="number of pages to crawl per type", dest="total_pages")

    args = parser.parse_args()
    main(**vars(args))
