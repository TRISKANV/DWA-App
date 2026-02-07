import pyperclip

def get_stashed_link():
    """Detecta si hay un link de video en el portapapeles"""
    text = pyperclip.paste()
    if any(site in text for site in ['youtube.com', 'tiktok.com', 'instagram.com', 'twitter.com']):
        return text
    return None
