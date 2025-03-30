from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    DATABASE_URL: str = "mysql+pymysql://root:0000@localhost/mr_patent"
    PROJECT_NAME: str = "특허 처리 API"
    
    class Config:
        env_file = ".env"

settings = Settings()
