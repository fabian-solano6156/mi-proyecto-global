from sqlalchemy.orm import Session
import models, dtos


def crear_practica(db: Session, practica: dtos.PracticaCreate):

    db_practica = models.Practica(
        nombre=practica.nombre,
        descripcion=practica.descripcion,
        tipo=practica.tipo,
        objetivos=practica.objetivos
    )

    db.add(db_practica)
    db.commit()
    db.refresh(db_practica)

   
    if practica.tipo == "electronica" and practica.electronica:

        det = practica.electronica

        db_det = models.PracticaElectronica(
            practica_id=db_practica.id,
            componentes=det.componentes,
            herramientas=det.herramientas,
            conexiones=det.conexiones,
            codigo=det.codigo,
            diagramas=det.diagramas,
            pasos=det.pasos
        )

        db.add(db_det)

    
    elif practica.tipo == "computo" and practica.computo:

        det = practica.computo

        db_det = models.PracticaComputo(
            practica_id=db_practica.id,
            aplicaciones=det.aplicaciones,
            dependencias=det.dependencias,
            comandos=det.comandos,
            archivos=det.archivos,
            pasos=det.pasos
        )

        db.add(db_det)

    db.commit()
    db.refresh(db_practica)

    return db_practica


def get_practicas(db: Session):
    return db.query(models.Practica).all()


def crear_archivo(db: Session, practica_id: int, archivo: dtos.ArchivoCreate):
    db_archivo = models.Archivo(**archivo.model_dump(), practica_id=practica_id)
    db.add(db_archivo)
    db.commit()
    db.refresh(db_archivo)
    return db_archivo

def eliminar_practica(db: Session, practica_id: int):
    practica = db.query(models.Practica).filter(models.Practica.id == practica_id).first()
    
    if practica:
        db.delete(practica)
        db.commit()
        return True
        
    return False

def actualizar_practica(db: Session, practica_id: int, practica_data: dtos.PracticaCreate):
    db_practica = db.query(models.Practica).filter(models.Practica.id == practica_id).first()
    
    if not db_practica:
        return None

    db_practica.nombre = practica_data.nombre
    db_practica.descripcion = practica_data.descripcion
    db_practica.tipo = practica_data.tipo
    db_practica.objetivos = practica_data.objetivos

    if practica_data.tipo == "electronica" and practica_data.electronica:
        det = practica_data.electronica
        
        db_det = db.query(models.PracticaElectronica).filter(models.PracticaElectronica.practica_id == practica_id).first()
        
        if db_det:
            db_det.componentes = det.componentes
            db_det.herramientas = det.herramientas
            db_det.conexiones = det.conexiones
            db_det.codigo = det.codigo
            db_det.diagramas = det.diagramas
            db_det.pasos = det.pasos
        else:
            nuevo_det = models.PracticaElectronica(
                practica_id=db_practica.id,
                componentes=det.componentes,
                herramientas=det.herramientas,
                conexiones=det.conexiones,
                codigo=det.codigo,
                diagramas=det.diagramas,
                pasos=det.pasos
            )
            db.add(nuevo_det)

    elif practica_data.tipo == "computo" and practica_data.computo:
        det = practica_data.computo
        
        db_det = db.query(models.PracticaComputo).filter(models.PracticaComputo.practica_id == practica_id).first()
        
        if db_det:
            db_det.aplicaciones = det.aplicaciones
            db_det.dependencias = det.dependencias
            db_det.comandos = det.comandos
            db_det.archivos = det.archivos
            db_det.pasos = det.pasos
        else:
            nuevo_det = models.PracticaComputo(
                practica_id=db_practica.id,
                aplicaciones=det.aplicaciones,
                dependencias=det.dependencias,
                comandos=det.comandos,
                archivos=det.archivos,
                pasos=det.pasos
            )
            db.add(nuevo_det)

    db.commit()
    db.refresh(db_practica)
    
    return db_practica

