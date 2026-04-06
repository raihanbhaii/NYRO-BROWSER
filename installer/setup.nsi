!include "MUI2.nsh"
!include "LogicLib.nsh"

!define APP_NAME "NyroBrowser"
!define OUTFILE "NyroBrowser-Setup.exe"
!define INSTALL_DIR "$PROGRAMFILES\${APP_NAME}"

!define MUI_WELCOMEFINISHPAGE_BITMAP "loading_screen.png"
!define MUI_ICON "app_icon.ico"
!define MUI_ABORTWARNING

Name "${APP_NAME}"
OutFile "${OUTFILE}"
InstallDir "${INSTALL_DIR}"
RequestExecutionLevel admin
ShowInstDetails nevershow

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_LANGUAGE "English"

Section "Main"
  SetOutPath "$INSTDIR"
  SetDetailsPrint none
  
  DetailPrint "Extracting Chromium Engine..."
  Sleep 800
  DetailPrint "Configuring Java Runtime..."
  Sleep 600
  DetailPrint "Creating Shortcuts..."
  Sleep 400
  
  File /r "dist\package\*.*"
  
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  CreateShortCut "$DESKTOP\${APP_NAME}.lnk" "$INSTDIR\bin\NyroBrowser.exe" "" "$INSTDIR\app_icon.ico"
  CreateDirectory "$SMPROGRAMS\${APP_NAME}"
  CreateShortCut "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk" "$INSTDIR\bin\NyroBrowser.exe" "" "$INSTDIR\app_icon.ico"
SectionEnd

Section "Uninstall"
  Delete "$INSTDIR\Uninstall.exe"
  RMDir /r "$INSTDIR"
  Delete "$DESKTOP\${APP_NAME}.lnk"
  RMDir "$SMPROGRAMS\${APP_NAME}"
SectionEnd
