#This file adds all the files in a directory to the SQLLite Database


import os
import sqlite3

path = "/home/pi/Documents/Readings/"
conn = sqlite3.connect('/home/pi/Documents/SmartMeter.db')
for filename in os.listdir(path):
	current_path = path+filename
	with open(current_path) as f:
		for line in f:
			values = line.split(',')
			
			c = conn.cursor()
			c.execute('INSERT INTO POWER_Table VALUES ("'+values[0]+'","'+values[1]+'","'+values[2] +'","'+values[3]+'");')
			conn.commit()
		
	os.remove(current_path)
conn.close()	
