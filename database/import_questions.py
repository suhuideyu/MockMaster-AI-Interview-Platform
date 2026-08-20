import pandas as pd
from sqlalchemy import create_engine

DB_PASSWORD = "root"

DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": DB_PASSWORD,
    "database": "mockmaster",
    "table": "resource"
}

df = pd.read_excel("questions.xlsx")

df_import = df[["resource_type", "title", "content", "url", "job_id", "difficulty"]]

engine = create_engine(
    f"mysql+pymysql://{DB_CONFIG['user']}:{DB_CONFIG['password']}@{DB_CONFIG['host']}/{DB_CONFIG['database']}?charset=utf8mb4"
)

df_import.to_sql(
    name=DB_CONFIG["table"],
    con=engine,
    if_exists="append",
    index=False
)

print("( •̀ ω •́ )✧导入成功！")