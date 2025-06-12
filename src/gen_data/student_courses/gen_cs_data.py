import pandas as pd
import numpy as np
import os


base_dir = os.path.dirname(os.path.abspath(__file__))


dataset = [5000, 1000, 200000]


student_path = os.path.join(base_dir, f"../probability_based_students/datasource/data_{dataset[0]}.csv")
course_path = os.path.join(base_dir, f"../spider_based_courses/course_data/course{dataset[1]}.csv")
output_path = os.path.join(base_dir, f"./enrollments_data/enrollments{dataset[2]}.csv")


students = pd.read_csv(student_path, dtype={"SID": str})
courses = pd.read_csv(course_path, dtype={"CID": str})


student_ids = students["SID"].unique()
course_ids = courses["CID"].unique()

target_records = dataset[2]


selected_pairs = set()
while len(selected_pairs) < target_records:
    temp = len(selected_pairs)
    if temp % 1000:
        print(temp)
    sids = np.random.choice(student_ids, size=target_records, replace=True)
    cids = np.random.choice(course_ids, size=target_records, replace=True)
    selected_pairs.update(zip(sids, cids))


enrollments = pd.DataFrame(list(selected_pairs)[:target_records], columns=["SID", "CID"])


grades = np.random.beta(a=5, b=2, size=target_records) * 100
grades = np.clip(grades, 30, 100).round(1)

null_indices = np.random.choice(target_records, size=int(0.05 * target_records), replace=False)
grades[null_indices] = np.nan

enrollments["GRADE"] = grades


enrollments.to_csv(output_path, index=False)
print(f"✅ 成功生成 {len(enrollments)} 条选课记录，已保存至: {output_path}")
