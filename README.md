Raspberry-Pi-RFID

This system uses a D-logic µFR Nano NFC RFID reader to continuously scan for for an  RFID 
tag so that the information is sent to a Databse. this information is read by a Web application
where the user can register his/her information to the system or the system validates the user card
If the card is acceptablble, a TCP connection is established with another raspberry pi where 
facial recognition is used to validate the person's face
This system also reads temperature data from a sensor and write on to the card. 

TO-Run 

cd ../dist
sudo java -jar RFIDSample.jar 
