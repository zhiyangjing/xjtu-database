import datetime
import json
import os
import random
from datetime import date
from collections import defaultdict
from typing import *

import pandas as pd
from scipy.stats import truncnorm


class DataGenerator:
    def __init__(self):
        self.json_source = "datasource/data.json"
        self.output_path = "datasource/data_%d.csv"

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
        return start_date + datetime.timedelta(days=random_offset)

    def gen_dorms(self, base: int) -> List[str]:
        res = []
        cnt = int(base / 4 * 1.5)
        for i in range(cnt):
            res.append(f"{random.choice('东西南北')}{int(i / (cnt / 2)) + 1}舍{(int((i % 359) / 40) + 1)}{i % 40 + 1:02}")
        return res

    def gen_names(self, cnt=5000, com_name_rate=0.1, minority_name_rate=0.02):
        available_dorm: List[str] = self.gen_dorms(cnt)
        used_dorm = defaultdict(int)
        print(os.getcwd())

        with open(self.json_source, encoding='utf-8') as data:
            name_data = json.load(data)
            surnames = list(name_data["surname"].keys())
            probabilities = list(name_data["surname"].values())
            com_surnames = name_data.get("com_surname", [])
            male_single = name_data["male_single"]
            female_single = name_data["female_single"]
            male_double = name_data.get("male_double", [])
            female_double = name_data.get("female_double", [])

            result = []
            for i in range(cnt):
                if i % 100 == 0:
                    print(f"Processing: {i}th")

                # 性别
                gender = random.choice(["男", "女"])
                sid = f"040{i:04}{0 if gender == '男' else 1}"

                # 姓氏：支持复姓，按比例插入
                if random.random() < com_name_rate:
                    surname = random.choice(com_surnames)
                else:
                    surname = random.choices(surnames, weights=probabilities, k=1)[0]

                # 姓名主体生成
                is_minority = random.random() < minority_name_rate
                if is_minority:
                    # 少数民族姓名风格
                    ethnic_prefix = random.choice(["耶利科特", "沙布尔", "纳木错", "哈萨尔", "格日勒图", "乌兰其其格"])
                    ethnic_suffix = random.choice(["别克", "巴图", "朝格图", "玛木提", "苏里登", "艾山"])
                    name = f"{ethnic_prefix}·{ethnic_suffix}"
                else:
                    k = 2 if random.random() > 0.02 else 1
                    if gender == "男":
                        name = random.choice(male_double) if k == 2 and male_double else "".join(random.choices(male_single, k=k))
                    else:
                        name = random.choice(female_double) if k == 2 and female_double else "".join(random.choices(female_single, k=k))

                fullname = surname + name

                # 出生日期
                birthdate = self.gen_date()

                # 身高模拟
                height = round(random.gauss(mu=173 if gender == "男" else 163, sigma=7), 2)

                # 宿舍分配
                dorm = random.choice(available_dorm)
                while used_dorm[dorm] >= 4:
                    available_dorm.remove(dorm)
                    dorm = random.choice(available_dorm)
                used_dorm[dorm] += 1

                result.append([sid, fullname, gender, height, birthdate, dorm])

            pd_data = pd.DataFrame(result)
            pd_data.to_csv(self.output_path % cnt, header=["SID", "SNAME", "SEX", "HEIGHT", "BDATE", "DORM"], index=False)


if __name__ == "__main__":
    generator = DataGenerator()
    generator.gen_names()
