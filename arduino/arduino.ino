#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <SPI.h>
#include <WiFi.h>
#include <dht.h>           // Note that the DHT file must be in your Arduino installation 
                           // folder, in the library foler.
#include <Servo.h> 
#define DhtPin   8         // This #define defines the pin that will be used to get data 
                           // from the tempurature & humidity sensor. 
                           
dht DHT;                   // This sets up an equivalence between dht and DHT.

Servo myservo;
int ServoPin = 9;          // Servo Pin
int DoorSwitchPin = 2;     // Pin the door switch is connected to
int LightPin = 5;          // Pin that the corner light LED is connected to
int AlarmPin = 3;          // Pin that the alarm inidicator LED is connected to
int QtiPin = 6;            // The pin with QTI/proximity sensor

#define MQTT_SERVER "iot.eclipse.org"
#define MQTT_CLIENT_ID "SA_NODE1"
#define WIFI_SSID "LGTeam1"

// Callback function header
char json[100];
void callback(char* topic, byte* payload, unsigned int length);

WiFiClient wifiClient;
PubSubClient client(MQTT_SERVER, 1883, callback, wifiClient);

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.println("Message arrived:  topic: " + String(topic));
  Serial.println("Length: " + String(length,DEC));

  StaticJsonBuffer<200> jsonBuffer;

  // Allocate the correct amount of memory for the payload copy
  memcpy(json, payload, length);
  JsonObject& root = jsonBuffer.parseObject(json);
  
  if (!root.success()) {
    Serial.println("parseObject() failed");
  } else {
    const char* sensor = root["sensor"];
    Serial.println(sensor);
    if (String(sensor) == String("closedoor")){
      CloseDoor();
    } else if(String(sensor) == String("opendoor")) {
      OpenDoor();
    }
  }
}

void setup() {
  Serial.begin(115200);
  myservo.attach(ServoPin);                     // Attach to servo

  WiFi.begin(WIFI_SSID);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  // Print connection information to the debug terminal
  printConnectionStatus();
  
  Serial.print("Connecting to ");
  Serial.println(MQTT_SERVER);
  
  if (client.connect(MQTT_CLIENT_ID)) {
    Serial.println("Connected to MQTT broker");

    if (!client.subscribe("/device/sanode1")){
      Serial.println("fail to subscribe!");
    }
  }
  else {
    Serial.println("MQTT connect failed");
    Serial.println("Will reset and try again...");
    abort();
  }
}

void loop() {
  client.loop();
}


/*********************************************************************
* void CloseDoor()
* Parameters: None           
* Global Variable: myservo
*
* Description: 
* This method uses a servo write command to set the servo to its 50%
* position - this is sufficient to close the door. The write method 
* converts the integer 90 into a pulse width modulated value to move 
* the servo to the mid-point.
*
* WARNING: do not change the servo write value... doing so could break 
* the door and/or servo.
***********************************************************************/

void CloseDoor()
{
  Serial.println( "Closing Door..." );
  myservo.write(90);  // Set servo to mid-point. This closes
                      // the door.
}  

/*********************************************************************
* void OpenDoor()
* Parameters: None           
* Global Variable: myservo
*
* Description: 
* This method uses a servo write command to set the servo to its full
* clockwise position - this is sufficient to open the door. The write 
* method converts the integer 0 into a pulse width modulated value to 
* move the servo.
*
* WARNING: do not change the servo write value... doing so could break 
* the door and/or servo.
***********************************************************************/

void OpenDoor()
{
  Serial.println( "Opening Door..." );
  myservo.write(0);  // Set servo to its full clockwise position.
                     // This opens the door
}

/*********************************************************************
* int DoorState(int Pin)
* Parameters:            
* int pin - the pin on the Arduino where the door switch is connected.
*
* Description: 
* This method reads the state of Pin to determine what the voltage level
* is. If the input is 5v it is high, and will return a 1 signifying that
* the door is closed. If the input is 0v it is low, and will return a 0
* signifying that the door is open.
***********************************************************************/

int DoorState( int Pin )
{
  int val = 0;
  val = digitalRead( Pin );
  return val;
}

/*********************************************************************
* void LedOn(int Pin)
* Parameters:            
* int pin - the pin on the Arduino where the LED is connected.
*
* Description: 
* This method writes a 1 to the specified pin. This places 5v on the Pin
* lighting the LED.
***********************************************************************/

void LedOn( int Pin )
{
   pinMode( Pin, OUTPUT);      // Set the specified pin to output mode.
   digitalWrite( Pin, HIGH);   // Set the pin to 5v.
}

/*********************************************************************
* void LedOff(int Pin)
* Parameters:            
* int pin - the pin on the Arduino where the LED is connected.
*
* Description: 
* This method writes a 0 to the specified pin. This places 0v on the Pin
* turning off the LED.
***********************************************************************/

void LedOff( int Pin )
{
   pinMode( Pin, OUTPUT);     // Set the pin to output mode.
   digitalWrite( Pin, LOW);   // Set the pin to 0 volts.
}

/*********************************************************************
* long ProximityVal(int Pin)
* Parameters:            
* int pin - the pin on the Arduino where the QTI sensor is connected.
*
* Description:
* QTI schematics and specs: http://www.parallax.com/product/555-27401
* This method initalizes the QTI sensor pin as output and charges the
* capacitor on the QTI. The QTI emits IR light which is reflected off 
* of any surface in front of the sensor. The amount of IR light 
* reflected back is detected by the IR resistor on the QTI. This is 
* the resistor that the capacitor discharges through. The amount of 
* time it takes to discharge determines how much light, and therefore 
* the lightness or darkness of the material in front of the QTI sensor.
* Given the closeness of the object in this application you will get
* 0 if the sensor is covered
***********************************************************************/
long ProximityVal(int Pin)
{
    long duration = 0;
    pinMode(Pin, OUTPUT);         // Sets pin as OUTPUT
    digitalWrite(Pin, HIGH);      // Pin HIGH
    pinMode(Pin, INPUT);          // Sets pin as INPUT
    digitalWrite(Pin, LOW);       // Pin LOW
    while(digitalRead(Pin) != 0)  // Count until the pin goes
       duration++;                // LOW (cap discharges)
       
    return duration;              // Returns the duration of the pulse
}

/************************************************************************************************
* The following method prints out the connection information
************************************************************************************************/

 void printConnectionStatus() 
 {
     long rssi;                     // The WIFI shield signal strength
     byte mac[6];                   // MAC address of the WIFI shield
 
     // Print the basic connection and network information: Network, IP, and Subnet mask
     Serial.print("Connected to ");
     Serial.println(WIFI_SSID);
     Serial.print(" IP Address:: ");
     Serial.println(WiFi.localIP());
     Serial.print("Netmask: ");
     Serial.println(WiFi.subnetMask());
   
     // Print our MAC address.
     WiFi.macAddress(mac);
     Serial.print("WiFi Shield MAC address: ");
     Serial.print(mac[5],HEX);
     Serial.print(":");
     Serial.print(mac[4],HEX);
     Serial.print(":");
     Serial.print(mac[3],HEX);
     Serial.print(":");
     Serial.print(mac[2],HEX);
     Serial.print(":");
     Serial.print(mac[1],HEX);
     Serial.print(":");
     Serial.println(mac[0],HEX);
   
     // Print the wireless signal strength:
     rssi = WiFi.RSSI();
     Serial.print("Signal strength (RSSI): ");
     Serial.print(rssi);
     Serial.println(" dBm");

 } // printConnectionStatus
 
