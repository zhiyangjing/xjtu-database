import datetime
import json
import random
from datetime import date
from collections import defaultdict

from matplotlib.style.core import available
from scipy.stats import truncnorm
from typing import *
import pandas as pd


class DataGenerator:
    def __init__(self):
        self.json_source = "./datasource/data.json"
        self.output_path_1000 = "./datasource/data_1000.csv"

    def gen_date(self) -> date:
        start_date = datetime.date(2003, 1, 1)
        end_date = datetime.date(2005, 12, 31)

        delta_days = (end_date - start_date).days

        mu_date = datetime.date(2004, 7, 1)
        mu_days = (mu_date - start_date).days
        sigma = 150

        a, b = (0 - mu_days) / sigma, (delta_days - mu_days) / sigma
        trunc_norm = truncnorm(a, b, loc=mu_days, scale=sigma)

        random_offset = int(trunc_norm.rvs())
        random_date = start_date + datetime.timedelta(days=random_offset)
        return random_date

    def gen_dorms(self, base) -> List[str]:
        res = []
        cnt = int(base / 4 * 1.5)
        for i in range(cnt):
            res.append(
                f"{random.choice(list('东西'))}{int(i / (cnt / 2)) + 1}舍{(int((i % 359) / 40) + 1)}{i % 40 + 1:02}")
        return res

    def gen_names(self, cnt=1000, com_name_rate=0.1):
        available_dorm: List[str] = self.gen_dorms(cnt)
        used_dorm = defaultdict(int)
        with open(self.json_source, encoding='utf-8') as data:
            name_data = json.load(data)
            surnames = []
            probabilities = []
            male_single = name_data["male_single"]
            female_single = name_data["female_single"]
            for key, val in name_data["surname"].items():
                surnames.append(key)
                probabilities.append(val)

            chosen_surnames = random.choices(surnames, weights=probabilities, k=cnt)
            result = []
            for i in range(cnt):
                gender = random.choice(["男", "女"])
                name = ""
                height = 1.7
                if gender == "男":
                    name = "".join(random.choices(male_single, k=2))
                    height = round(random.gauss(mu=173, sigma=7))
                else:
                    name = "".join(random.choices(female_single, k=2))
                    height = round(random.gauss(mu=163, sigma=7), 2)
                fullname = chosen_surnames[i] + name
                birthdate = self.gen_date()
                dorm = random.choice(available_dorm)
                while used_dorm[dorm] >= 4:
                    available_dorm.remove(dorm)
                    dorm = random.choice(available_dorm)
                used_dorm[dorm] += 1
                result.append([fullname, gender, height, birthdate, dorm])
            pd_data: pd.DataFrame = pd.DataFrame(result)
            pd_data.to_csv(self.output_path_1000, header=["SNAME", "SEX", "HEIGHT", "BDATE", "DORM"])


if __name__ == "__main__":
    generator = DataGenerator()
    generator.gen_names()
