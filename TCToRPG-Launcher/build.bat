@echo off
setlocal
python -m pip install -r requirements.txt
python -m pip install pyinstaller
pyinstaller --onefile --windowed --name TCToRPG-Launcher launcher.py
endlocal
