from pydantic import BaseModel
from typing import List, Optional, Any, Dict


class ArchivoBase(BaseModel):
    nombre: str
    ruta: str
    tipo_archivo: str
    extension: str


class ArchivoCreate(ArchivoBase):
    pass


class ArchivoResponse(ArchivoBase):
    id: int
    practica_id: int

    class Config:
        from_attributes = True


class DetallesElectronica(BaseModel):
    componentes: List[str] = []
    herramientas: List[str] = []
    conexiones: List[Dict[str, Any]] = []
    codigo: List[Dict[str, Any]] = []
    diagramas: List[Dict[str, Any]] = []
    pasos: List[str] = []


class DetallesComputo(BaseModel):
    aplicaciones: List[str] = []
    dependencias: List[str] = []
    comandos: List[Dict[str, Any]] = []
    archivos: List[Dict[str, Any]] = []
    pasos: List[str] = []


class PracticaCreate(BaseModel):
    nombre: str
    descripcion: str
    tipo: str
    objetivos: List[str] = []

    electronica: Optional[DetallesElectronica] = None
    computo: Optional[DetallesComputo] = None


class PracticaResponse(BaseModel):
    id: int
    nombre: str
    descripcion: str
    tipo: str
    objetivos: List[str]

    detalles_electronica: Optional[DetallesElectronica] = None
    detalles_computo: Optional[DetallesComputo] = None
    archivos: List[ArchivoResponse] = []

    class Config:
        from_attributes = True