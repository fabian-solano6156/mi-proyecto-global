from sqlalchemy import Column, Integer, String, ForeignKey, Text
from sqlalchemy.orm import relationship
from database import Base
from sqlalchemy.types import JSON


class Practica(Base):
    __tablename__ = "practicas"

    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String)
    descripcion = Column(Text)
    tipo = Column(String) 
    objetivos = Column(JSON)

    detalles_electronica = relationship(
        "PracticaElectronica",
        back_populates="practica",
        uselist=False,
        cascade="all, delete-orphan"
    )

    detalles_computo = relationship(
        "PracticaComputo",
        back_populates="practica",
        uselist=False,
        cascade="all, delete-orphan"
    )

    archivos = relationship(
        "Archivo",
        back_populates="practica",
        cascade="all, delete-orphan"
    )


class PracticaElectronica(Base):
    __tablename__ = "practicas_electronica"

    id = Column(Integer, primary_key=True)
    practica_id = Column(Integer, ForeignKey("practicas.id"))

    componentes = Column(JSON)
    herramientas = Column(JSON)
    conexiones = Column(JSON)
    codigo = Column(JSON)
    diagramas = Column(JSON)
    pasos = Column(JSON)

    practica = relationship("Practica", back_populates="detalles_electronica")


class PracticaComputo(Base):
    __tablename__ = "practicas_computo"

    id = Column(Integer, primary_key=True)
    practica_id = Column(Integer, ForeignKey("practicas.id"))

    aplicaciones = Column(JSON)
    dependencias = Column(JSON)
    comandos = Column(JSON)
    pasos = Column(JSON)
    archivos = Column(JSON)

    practica = relationship("Practica", back_populates="detalles_computo")


class Archivo(Base):
    __tablename__ = "archivos"

    id = Column(Integer, primary_key=True)
    practica_id = Column(Integer, ForeignKey("practicas.id"))

    nombre = Column(String)
    ruta = Column(String)
    tipo_archivo = Column(String)
    extension = Column(String)

    practica = relationship("Practica", back_populates="archivos")