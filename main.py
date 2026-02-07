from core.downloader import DWADownloader
from utils.clipboard import get_stashed_link

def iniciar_dwa():
    print("--- 🚀 DWA (Download Video App) ---")
    
    # 1. Detectar portapapeles
    link = get_stashed_link()
    if link:
        print(f"📌 Detectamos un link: {link}")
        confirm = input("¿Querés analizar este video? (s/n): ")
        if confirm.lower() != 's': link = input("Pegá el link manualmente: ")
    else:
        link = input("Pegá el link aquí: ")

    # 
    dwa = DWADownloader()
    print("🔍 Analizando opciones de calidad...")
    info = dwa.fetch_info(link)
    
    print(f"\n🎥 Video: {info['title']}")
    for i, f in enumerate(info['formats']):
        print(f"[{i}] {f['res']}p - Formato: {f['ext']} ({f['note']})")
    
    # 3. Descargar
    choice = int(input("\nElegí el número de opción: "))
    selected_format = info['formats'][choice]['id']
    
    print("⏳ Descargando... (Se guardará en /DWA_Downloads)")
    # 

if __name__ == "__main__":
    iniciar_dwa()
