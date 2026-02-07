import yt_dlp
import os

class DWADownloader:
    def __init__(self):
        # Tu Plan B: Carpeta propia
        self.output_dir = 'DWA_Downloads'
        if not os.path.exists(self.output_dir):
            os.makedirs(self.output_dir)

    def fetch_info(self, url):
        """Analiza el link y nos da las opciones de calidad"""
        ydl_opts = {'quiet': True, 'noplaylist': True}
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=False)
            # 
            formats = [
                {'id': f['format_id'], 'ext': f['ext'], 'res': f.get('height'), 'note': f.get('format_note')}
                for f in info.get('formats', []) if f.get('height')
            ]
            return {
                'title': info.get('title'),
                'thumbnail': info.get('thumbnail'),
                'formats': formats[:5] 
            }
