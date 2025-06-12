from selenium import webdriver
from selenium.webdriver.edge.service import Service
from selenium.webdriver.edge.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time


def init_driver(headless=False) -> webdriver.Edge:
    options = Options()
    if headless:
        options.add_argument("--headless")
    options.add_argument("--disable-gpu")
    options.add_argument("--window-size=1920,1080")

    service = Service(executable_path="D:/applications/tools/scoop/apps/edgedriver/current/msedgedriver.exe")
    driver = webdriver.Edge(service=service, options=options)
    return driver


def scroll_to_bottom(driver):
    print("Start scrolling to bottom...")
    last_height = driver.execute_script("return document.body.scrollHeight")
    while True:
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(1.5)
        new_height = driver.execute_script("return document.body.scrollHeight")
        if new_height == last_height:
            break
        last_height = new_height
    print("Scrolling finished")


def get_course_data(driver, pages=20) -> list:
    url = "https://www.icourse163.org/channel/2001.htm"
    driver.get(url)

    print("Page loaded, start scrolling...")
    scroll_to_bottom(driver)
    print("Scrolling done, start finding elements...")

    wait = WebDriverWait(driver, 15)
    result = []

    for page_index in range(pages):
        print(f"Processing page {page_index + 1}...")

        wait.until(EC.presence_of_element_located((By.ID, "channel-course-list")))
        container = driver.find_element(By.ID, "channel-course-list")
        course_blocks = container.find_elements(By.CLASS_NAME, "_1Bfx4")

        classes = []
        for course in course_blocks:
            try:
                title = course.find_element(By.CSS_SELECTOR, "h3._3EwZv._1VDzh").text.strip()
            except:
                title = "（无标题）"
            try:
                teacher = course.find_element(By.CSS_SELECTOR, "div._1Zkj9").text.strip()
            except:
                teacher = "（无教师）"
            classes.append([title, teacher])

        result.extend(classes)
        print(*classes, sep="\n")

        # 翻页逻辑（跳过最后一页）
        if page_index < pages - 1:
            try:
                pagination = driver.find_element(By.CLASS_NAME, "_1lKzE")
                a_tags = pagination.find_elements(By.TAG_NAME, "a")
                next_button = a_tags[-1]  # 最后一个 <a> 是“下一页”

                # 点击前先记录旧容器用于等待页面刷新
                driver.execute_script("arguments[0].click();", next_button)

                # 等待页面刷新
                print("Waiting for new content...")
                wait.until(EC.presence_of_element_located((By.CLASS_NAME, "_1Bfx4")))
                scroll_to_bottom(driver)
                print("Waiting finished.")

            except Exception as e:
                print(f"翻页失败（第 {page_index + 1} 页后）：", e)
                break

    return result


def main(close_browser=False):
    driver = init_driver()
    try:
        data = get_course_data(driver, pages=5)
        print("Get finished...")
        with open("./course_data/course100.txt", "w", encoding="utf-8") as file:
            file.write("[")
            file.write(",\n".join(map(str, data)))
            file.write("]")
    finally:
        if close_browser:
            driver.quit()


if __name__ == "__main__":
    main(close_browser=False)
