import random
from typing import *
import pandas as pd

# class_type = ["CS", "EE", "ME", "MATH", "PHYS", "CHEM", "BIO", "ECON", "PSY", "HIST"]

class_type = ["CS", "EE", "ME", "MATH", "PHYS", "CHEM", "BIO", "ECON", "PSY", "HIST", "STAT", "PHIL", "SOC", "ART",
              "MUS", "SE", "CE", "BME", "ENV", "LAW"]

# cnt = 100
cnt = 1000


def to_csv():
    with open(f"./course_data/course{cnt}.txt", encoding="utf-8") as file:
        data = "".join(file.readlines())
        list_data: List[List[str]] = eval(data)
        tot = len(list_data)
        tot_type = len(class_type)
        res = pd.DataFrame(columns=["CID", "CNAME", "PERIOD", "CREDIT", "TEACHER"])
        for i in range(tot_type):
            for j in range(tot // tot_type):
                cid = f"{class_type[i]}-{j + 1:02}"
                cname = list_data[i * (tot // tot_type) + j][0]
                teacher = list_data[i * (tot // tot_type) + j][1]
                period, credit = random.choices(
                    [[4, 0.5], [16, 1], [32, 2], [48, 2.5], [64, 4]],
                    weights=[0.05, 0.15, 0.3, 0.25, 0.25],
                    k=1
                )[0]

                # 正确添加一行
                res.loc[len(res)] = [cid, cname, period, credit, teacher]
        res.to_csv(f"./course_data/course{cnt}.csv", index=False)


def main():
    to_csv()


if __name__ == "__main__":
    main()
