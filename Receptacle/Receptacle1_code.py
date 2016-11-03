##:'######:::'########:::'#######::'##::::'##:'########:::::'########::'######:::
##'##... ##:: ##.... ##:'##.... ##: ##:::: ##: ##.... ##:::: ##.....::'##... ##::
## ##:::..::: ##:::: ##: ##:::: ##: ##:::: ##: ##:::: ##:::: ##::::::: ##:::..:::
## ##::'####: ########:: ##:::: ##: ##:::: ##: ########::::: #######:: ##::'####:
## ##::: ##:: ##.. ##::: ##:::: ##: ##:::: ##: ##.....::::::...... ##: ##::: ##::
## ##::: ##:: ##::. ##:: ##:::: ##: ##:::: ##: ##:::::::::::'##::: ##: ##::: ##::
##. ######::: ##:::. ##:. #######::. #######:: ##:::::::::::. ######::. ######:::
##:......::::..:::::..:::.......::::.......:::..:::::::::::::......::::......::::

#This is the code for the Smart Receptacle Raspberry Pi
import RPi.GPIO as GPIO
import time
import ast
import os
import Adafruit_GPIO.SPI as SPI
import Adafruit_MCP3008
from time import sleep
from uuid import getnode


global CumPower1
global CumPower2
global Name1
global Name2
global PowerOn1
global PowerOn2
global MAC
global meterip
global meter_domain
global values

values= []
Name1 = ""
Name2 = ""
CumPower1 = 0
CumPower2 = 0
PowerOn1=True
PowerOn2=True
MAC = str(getnode())
meterip = "192.168.25.88" #Smart Meters IP
meter_domain = "pi"

def GetADCVoltage(value,ADCValues=[]):
        #Here we get the voltage for plug 1 and 2 from the ACD
        if (value == 1):
                return ((ADCValues[2]/1023.0)*3.3)
        else: 
                return ((ADCValues[0]/1023.0)*3.3)

def GetADCCurrent(value,ADCValues=[]):
        #Here we get the current for plug 1 and 2 from the ACD
        if (value == 1):
                return (((ADCValues[1]/1023.0)*3.3)/13.33)
        else:
                return (((ADCValues[3]/1023.0)*3.3)/13.33)

def OutputValue(name,value):
        print name + " :" +str(value)
        #prints the value on screen
def OutputKey():
        
        #OutHash = str(MAC)[0:3] +"-"+str(MAC)[3:6]+"-"+str(MAC)[6:9]+"-"+str(MAC)[9:11]
         #prints the hash on screen
        return MAC 

def getConfig():
         #coppies the config file from the meter to get the settings for each plug and updates the variables 
        global MAC
        global Name1
        global Name2
        global PowerOn1
        global PowerOn2
        config_dir= "/home/pi/Documents/Meter_Files/MasterConfig.cfg"
        os.system("scp "+meter_domain+"@"+meterip+":/home/"+meter_domain+"/Documents/Configuration/MasterConfig.cfg " + config_dir)
        try:
                with open(config_dir) as f:
                        for line in f:
                                values = line.split(',')
                                if (values[0] == MAC):
                                        if values[1] == "Name1":
                                                Name1=values[2]
                                                PowerOn1=ast.literal_eval(values[3])
                                        elif values[1]=="Name2":
                                                Name2=values[2]
                                                PowerOn2=ast.literal_eval(values[3])

                                else:
                                        Name1 = "Key 1 :" + OutputKey()
                                        Name2 = "Key 2 :" + OutputKey() 
        except IOError:
                Name1 = "Key 1 :" + OutputKey()
                Name2 = "Key 2 :" + OutputKey()
                print "IOError"

def ChangeONOFF():
        #Turns the circuit on and off depending on the values in the variables
        if PowerOn2:              
                GPIO.output(5, False)
                print "Power 2 ON"
        elif (not PowerOn2):
                GPIO.output(5, True)
                print "Power 2 OFF"
        if PowerOn1:
                print "Power 1 ON"
                GPIO.output(3, False)
        elif (not PowerOn1):
                print "Power 1 OFF"
                GPIO.output(3, True)


def SendValueToMeter(power1,power2):
        #sends power readings to the meters
        global Name1
        global Name2
        global MAC
        cur_time = time.strftime("%H_%M_%S")
        cur_date = time.strftime("%m_%d_%Y")
        file = open("/home/pi/Documents/Meter_Files/Reading1_Temp.txt", "w")
        file.write(str(Name1+","+str(power1)+","+cur_date+","+cur_time))
        file.write("\n")
        file.write(str(Name2+","+str(power2)+","+cur_date+","+cur_time))
        file.close()
        os.system("scp /home/pi/Documents/Meter_Files/Reading1_Temp.txt  "+meter_domain+"@"+meterip+":/home/"+meter_domain+"/Documents/Readings/"+str(MAC)+"_Reading_"+cur_time+"_"+cur_date)
        os.system("ssh "+meter_domain+"@"+meterip+" python /home/"+meter_domain+"/Documents/db_files.py")
#Initalizing GPIO Pins
GPIO.setmode(GPIO.BOARD)   
GPIO.setup(3, GPIO.OUT)
GPIO.setup(5, GPIO.OUT)

#Setting Up ADC
SPI_PORT   = 0
SPI_DEVICE = 0
mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))

while True:        
#main task     
        getConfig()
        ChangeONOFF()
        values = [0]*8
        for i in range(8):
                values[i] = mcp.read_adc(i)
        print GetADCVoltage(1,values)
        print GetADCVoltage(2,values)
        print GetADCCurrent(1,values)
        print GetADCCurrent(2,values)
        power1 = GetADCCurrent(1,values) * GetADCVoltage(1,values)
        power2 = GetADCCurrent(2,values) * GetADCVoltage(2,values)
        CumPower1 += power1
        CumPower2 += power2
        SendValueToMeter(CumPower1,CumPower2)
        OutputValue(Name1,CumPower1)
        sleep(3)
        OutputValue(Name2,CumPower2)
        sleep(3)
        os.system("clear")


