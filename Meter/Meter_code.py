##:'######:::'########:::'#######::'##::::'##:'########:::::'########::'######:::
##'##... ##:: ##.... ##:'##.... ##: ##:::: ##: ##.... ##:::: ##.....::'##... ##::
## ##:::..::: ##:::: ##: ##:::: ##: ##:::: ##: ##:::: ##:::: ##::::::: ##:::..:::
## ##::'####: ########:: ##:::: ##: ##:::: ##: ########::::: #######:: ##::'####:
## ##::: ##:: ##.. ##::: ##:::: ##: ##:::: ##: ##.....::::::...... ##: ##::: ##::
## ##::: ##:: ##::. ##:: ##:::: ##: ##:::: ##: ##:::::::::::'##::: ##: ##::: ##::
##. ######::: ##:::. ##:. #######::. #######:: ##:::::::::::. ######::. ######:::
##:......::::..:::::..:::.......::::.......:::..:::::::::::::......::::......::::

#This is the code for the Smart Meter Raspberry Pi

import hashlib
import time
import os 
import dropbox
import datetime
from time import sleep
import sqlite3
from uuid import getnode
import RPi.GPIO as GPIO

##LCD PINS
LCD_RS = 15
LCD_E  = 16
LCD_D4 = 21
LCD_D5 = 22
LCD_D6 = 23
LCD_D7 = 24
LED_ON = 26

LCD_LINE_1 = 0x00# LCD RAM address for the 1st line
LCD_LINE_2 = 0xC0 # LCD RAM address for the 2nd line
LCD_LINE_3 = 0x90 # LCD RAM address for the 3rd line
LCD_LINE_4 = 0xD0 # LCD RAM address for the 4th line

E_PULSE = 0.0005
E_DELAY = 0.0005

LCD_WIDTH = 16    # Maximum characters per line
LCD_CHR = True
LCD_CMD = False

##GLOBALS
CumPower = 0
Highest_Cons_Name = " "
Highest_Cons_Value = 0
dbx = dropbox.Dropbox("aFhFx-elzFAAAAAAAAAAC_Wiu6wqOUS51LAIX9TiuHMa8Nf4N4NTw-CGGr5EULMd")
MAC = MAC = int(getnode())
MAC_hash = hashlib.md5(str(MAC)).hexdigest()

   
def lcd_init():
        # Initialise display
        lcd_byte(0x33,LCD_CMD) # 110011 Initialise
        time.sleep(0.005)
        lcd_byte(0x32,LCD_CMD) # 110010 Initialise
        time.sleep(0.005)
        lcd_byte(0x06,LCD_CMD) # 000110 Cursor move direction
        time.sleep(0.005)
        lcd_byte(0x0C,LCD_CMD) # 001100 Display On,Cursor Off, Blink Off
        time.sleep(0.005)
        lcd_byte(0x28,LCD_CMD) # 101000 Data length, number of lines, font size
        time.sleep(0.005)
        lcd_byte(0x01,LCD_CMD) # 000001 Clear display
def lcd_byte(bits, mode):
        # Send byte to data pins
        # bits = data
        # mode = True  for character
        #        False for command
         
        GPIO.output(LCD_RS, mode) # RS
         
        # High bits
        GPIO.output(LCD_D4, False)
        GPIO.output(LCD_D5, False)
        GPIO.output(LCD_D6, False)
        GPIO.output(LCD_D7, False)
        if bits&0x10==0x10:
                GPIO.output(LCD_D4, True)

        if bits&0x20==0x20:
                GPIO.output(LCD_D5, True)
        if bits&0x40==0x40:
                GPIO.output(LCD_D6, True)
        if bits&0x80==0x80:
                GPIO.output(LCD_D7, True)
         
          # Toggle 'Enable' pin
        lcd_toggle_enable()
         
          # Low bits
        GPIO.output(LCD_D4, False)
        GPIO.output(LCD_D5, False)
        GPIO.output(LCD_D6, False)
        GPIO.output(LCD_D7, False)
        if bits&0x01==0x01:
                GPIO.output(LCD_D4, True)
        if bits&0x02==0x02:
                GPIO.output(LCD_D5, True)
        if bits&0x04==0x04:
                GPIO.output(LCD_D6, True)
        if bits&0x08==0x08:
                GPIO.output(LCD_D7, True)
         
          # Toggle 'Enable' pin
        lcd_toggle_enable()
def lcd_toggle_enable():
        # Toggle enable
        time.sleep(E_DELAY)
        GPIO.output(LCD_E, True)
        time.sleep(E_PULSE)
        GPIO.output(LCD_E, False)
        time.sleep(E_DELAY)

def lcd_string(message,line,style):
        # Send string to display
        # style=1 Left justified
        # style=2 Centred
        # style=3 Right justified
         
        if style==1:
                message = message.ljust(LCD_WIDTH," ")
        elif style==2:
                message = message.center(LCD_WIDTH," ")
        elif style==3:
                message = message.rjust(LCD_WIDTH," ")
         
        lcd_byte(line, LCD_CMD)
         
        for i in range(LCD_WIDTH):
                lcd_byte(ord(message[i]),LCD_CHR)
def lcd_backlight(flag):
  # Toggle backlight on-off-on
        GPIO.output(LED_ON, flag)
 
def Init():
        #Initalizes the Meter and Displays Splash Screen on LCD
        lcd_byte(0x01, LCD_CMD)
        time.sleep(1) 
        lcd_string("Smart Meter",LCD_LINE_1,1)
        lcd_string("Smart Receptacle",LCD_LINE_2,1)
        lcd_string("",LCD_LINE_3,1)
        lcd_string("Team 5G",LCD_LINE_4,2)
        time.sleep(3)
        try:
                conn = sqlite3.connect('SmartMeter.db')
                c = conn.cursor()
                c.execute('SELECT * FROM MAC_Table')
                c.execute('SELECT * FROM IP_Table')
                conn.commit()
                conn.close()
        except sqlite3.OperationalError:
                CreateDb()

def OutputKey():
        #Puts Dashes In MAC Address for Easy Reading
        OutHash = str(MAC)[0:3] +"-"+str(MAC)[3:6]+"-"+str(MAC)[6:9]+"-"+str(MAC)[9:11]
        return OutHash 

def CreateDb():
        #Creates DB if No Db is found
        print "CreateDb"
        conn = sqlite3.connect('SmartMeter.db')
        c = conn.cursor()
        try:
                c.execute('''CREATE TABLE MAC_Table
                             (MAC text, NAME text)''')
        except sqlite3.OperationalError:
                print "MAC Table Already Exist"
        try:
                c.execute('''CREATE TABLE POWER_Table
                             (NAME text, POWER real,DATE text, TIME text)''')
        except sqlite3.OperationalError:
                print "Power Table Already Exist"

        conn.commit()
        conn.close()


def ExportDatabase():
        #Exports Databse to File
        global MAC_hash
        cur_time = time.strftime("%H_%M_%S")
        cur_date = time.strftime("%m_%d_%Y")
        conn = sqlite3.connect('SmartMeter.db')
        c = conn.cursor()
        Items = c.execute('SELECT * FROM POWER_Table;')
        filename = "/home/pi/Documents/db_exports/"+MAC_hash+"_Readings.txt"
        file = open(filename, "w")
        for item in Items:
                if (str(item[0]).strip != "") and(str(item[1]).strip != "") and(str(item[2]).strip != "") and(str(item[3]).strip != ""):
                        file.write("\n" + str(item[0])+","+str(item[1])+","+str(item[2])+","+str(item[3]))
        file.close()
        conn.close()

def DisplayTotal():
        #Calculates the Cumulative Power and Highest Consumer
        #Also Saves the Cumulative Power in DB as CumPower
        #Diaplays Power and Highest Consumer on LCD Screen
        global CumPower
        global Highest_Cons_Name
        global Highest_Cons_Value
        receptacle_names = []
        conn = sqlite3.connect('SmartMeter.db')
        c = conn.cursor()
        Items = c.execute('SELECT * FROM POWER_Table;')
        for value in Items:
                if ((value[0] not in receptacle_names) and (value[0] != "CumPower")):
                        receptacle_names.append(value[0])
                if (int(value[1]) > Highest_Cons_Value) and (value[0] != "CumPower"):
                        Highest_Cons_Name = value[0]
                        Highest_Cons_Value = value[1]
        print "Highest Consumer :"+ str(Highest_Cons_Name)
        config_location = "/home/pi/Documents/Configuration/MasterConfig.cfg"
        CumPower = 0.0
        for name in receptacle_names:
                LargestValue = 0
                Items = c.execute('select * from POWER_Table where NAME=\"'+name+'\"')
                for item in Items:
                        if (item[1] > LargestValue) and (item[0] != "CumPower") :
                                LargestValue = item[1]
                CumPower += LargestValue
        cur_time = time.strftime("%H_%M_%S")
        cur_date = time.strftime("%m_%d_%Y")
        c.execute('INSERT INTO POWER_Table VALUES ("'+"CumPower"+'","'+str(CumPower)+'","'+cur_date +'","'+cur_time+'");')
        conn.commit()
        conn.close()
        lcd_byte(0x01, LCD_CMD) #clears screen
        time.sleep(1) 
        print "TotalPower :" + str(round (CumPower,2))
        lcd_string("Total Power",LCD_LINE_1,1)
        lcd_string(str(round (CumPower,2))+" W",LCD_LINE_2,1)
        lcd_string("Top Consumer:",LCD_LINE_3,1)
        lcd_string(str(Highest_Cons_Name),LCD_LINE_4,1)
        


def UploadDatabaseExportFiles():
        #Uploads Exported File to the Dropbox Overwriting the exsisting file. 
        #the Hash is generated from the mac address of the pi and appended to the filename with the string "_Readings.txt"
        global dbx
        folder = "/home/pi/Documents/db_exports/"
        for filename in os.listdir(folder):
                current_path = folder+filename
                overwrite = True
                path = "//"+filename
        while '//' in path:
                path = path.replace('//', '/')
                mode = (dropbox.files.WriteMode.overwrite
                                if overwrite
                                else dropbox.files.WriteMode.add)
                mtime = os.path.getmtime(current_path)
                with open(current_path, 'rb') as f:
                        data = f.read()
                        try:
                                res = dbx.files_upload(
                                data, path, mode,
                                client_modified=datetime.datetime(*time.gmtime(mtime)[:6]),
                                mute=True)
                                os.remove(folder+filename)
                        except dropbox.exceptions.ApiError as err:
                                print('*** API error', err)
                        return None
                        print('uploaded as', res.name.encode('utf8'))
        return True

def downloadConfigFile():
                #Looks for a config file matching the hash of this pi
        try:
                global dbx
                path = "//"+MAC_hash+"_"+"MasterConfig.cfg"
                print path
                while '//' in path:
                        path = path.replace('//', '/')

                try:
                        md, res = dbx.files_download(path)
                except dropbox.exceptions.HttpError as err:
                        print('*** HTTP error', err)
                        return None
                data = res.content
                file = open("/home/pi/Documents/Configuration/MasterConfig.cfg", "w")
                file.write(data)
                file.close()
                return True
        except dropbox.exceptions.ApiError:
                print "Dropbox ConfigFile Not Found, Use App To Set Up"
                lcd_byte(0x01, LCD_CMD)
                time.sleep(1) 
                print "TotalPower :" + str(CumPower)
                lcd_string("Use App To Setup",LCD_LINE_1,1)
                lcd_string("",LCD_LINE_2,1)
                lcd_string("Meter Key",LCD_LINE_3,1)
                lcd_string(str(MAC),LCD_LINE_4,1)
                print OutputKey()
#LCD Display setup 
GPIO.setmode(GPIO.BOARD)    # Use BCM GPIO numbers
GPIO.setup(LCD_E, GPIO.OUT)  # E
GPIO.setup(LCD_RS, GPIO.OUT) # RS
GPIO.setup(LCD_D4, GPIO.OUT) # DB4
GPIO.setup(LCD_D5, GPIO.OUT) # DB5
GPIO.setup(LCD_D6, GPIO.OUT) # DB6
GPIO.setup(LCD_D7, GPIO.OUT) # DB7
GPIO.setup(LED_ON, GPIO.OUT) # Backlight enable
lcd_init()
#Toggle Backlight
lcd_backlight(True)
time.sleep(0.5)
lcd_backlight(False)
time.sleep(0.5)
lcd_backlight(True)
time.sleep(0.5)
Init()
while True:
        DisplayTotal()
        ExportDatabase()
        UploadDatabaseExportFiles()
        downloadConfigFile()
        sleep(2)

        
