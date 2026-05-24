from fastapi import FastAPI, Depends, HTTPException, UploadFile, File
from fastapi.staticfiles import StaticFiles 
from sqlalchemy.orm import Session
from typing import List
import models, dtos, crud, database
import shutil
import os

models.Base.metadata.create_all(bind=database.engine)

app = FastAPI(title="API de Prácticas Electrónica y Cómputo")

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)
app.mount("/uploads", StaticFiles(directory=UPLOAD_DIR), name="uploads")

def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()


@app.post("/practicas", response_model=dtos.PracticaResponse)
def crear_practica(practica: dtos.PracticaCreate, db: Session = Depends(get_db)):

   
    if practica.tipo == "electronica" and not practica.electronica:
        raise HTTPException(status_code=400, detail="faltan datos de electrónica")

    if practica.tipo == "computo" and not practica.computo:
        raise HTTPException(status_code=400, detail="faltan datos de cómputo")

    return crud.crear_practica(db=db, practica=practica)


@app.get("/practicas", response_model=List[dtos.PracticaResponse])
def listar_practicas(db: Session = Depends(get_db)):
    return crud.get_practicas(db)


@app.get("/practicas/{practica_id}", response_model=dtos.PracticaResponse)
def obtener_practica(practica_id: int, db: Session = Depends(get_db)):
    practica = db.query(models.Practica).filter(models.Practica.id == practica_id).first()

    if not practica:
        raise HTTPException(status_code=404, detail="práctica no encontrada")

    return practica

@app.delete("/practicas/{practica_id}")
def eliminar_practica(practica_id: int, db: Session = Depends(get_db)):

    exito = crud.eliminar_practica(db, practica_id)

    if not exito:
        raise HTTPException(status_code=404, detail="no se pudo eliminar")

    return {"message": "práctica eliminada correctamente"}


UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@app.post("/practicas/{practica_id}/subir-archivo")
async def subir_archivo(
    practica_id: int,
    file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    practica = db.query(models.Practica).filter(models.Practica.id == practica_id).first()
    if not practica:
        raise HTTPException(status_code=404, detail="Práctica no existe")

    filename = f"{practica_id}_{file.filename}"
    file_path = os.path.join(UPLOAD_DIR, filename)

    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    ext = os.path.splitext(file.filename)[1].lower()
    tipo = "imagen" if ext in [".png", ".jpg", ".jpeg", ".webp"] else "codigo"
    
    nuevo_archivo = models.Archivo(
        nombre=file.filename,
        ruta=filename,
        tipo_archivo=tipo,
        extension=ext,
        practica_id=practica_id
    )
    
    db.add(nuevo_archivo)
    db.commit()
    
    return {"message": "Archivo subido y registrado", "ruta": filename}


@app.put("/practicas/{practica_id}", response_model=dtos.PracticaResponse)
def actualizar_practica(practica_id: int, practica: dtos.PracticaCreate, db: Session = Depends(get_db)):
    
   
    if practica.tipo == "electronica" and not practica.electronica:
        raise HTTPException(status_code=400, detail="faltan datos de electrónica")

    practica_actualizada = crud.actualizar_practica(db=db, practica_id=practica_id, practica_data=practica)
    
    if not practica_actualizada:
        raise HTTPException(status_code=404, detail="práctica no encontrada")

    return practica_actualizada

@app.delete("/archivos/{archivo_id}")
def eliminar_archivo(archivo_id: int, db: Session = Depends(get_db)):
    # 1. Buscar el archivo en la base de datos
    db_archivo = db.query(models.Archivo).filter(models.Archivo.id == archivo_id).first()
    
    if not db_archivo:
        raise HTTPException(status_code=404, detail="Archivo no encontrado")
        
    
    file_path = os.path.join(UPLOAD_DIR, db_archivo.ruta)
    if os.path.exists(file_path):
        os.remove(file_path)
        
    
    db.delete(db_archivo)
    db.commit()
    
    return {"message": "Archivo eliminado correctamente"}