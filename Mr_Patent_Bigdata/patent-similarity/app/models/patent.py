# app/models/patent.py
from sqlalchemy import Column, Integer, String, Text, LargeBinary
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

# app/models/patent.py 수정
class Patent(Base):
    __tablename__ = "patents"
    
    id = Column(Integer, primary_key=True, index=True)
    # application_number = Column(String(20), unique=True, index=True, nullable=True)
    title = Column(String(255), nullable=False)
    content = Column(Text, nullable=False)
    tfidf_vector = Column(LargeBinary)
    kobert_vector = Column(LargeBinary)

