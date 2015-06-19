/*********************************************************************************************
 Include Header
**********************************************************************************************/
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <SPI.h>
#include <WiFi.h>
#include <dht.h>           // Note that the DHT file must be in your Arduino installation 
                           // folder, in the library foler.                         
#include <Servo.h>


/*********************************************************************************************
 Define Macro and Enum
**********************************************************************************************/

#define DhtPin   8         // This #define defines the pin that will be used to get data 
                           // from the tempurature & humidity sensor.                            
//#define MQTT_SERVER "iot.eclipse.org"
#define MQTT_CLIENT_ID "SA_NODE01"
#define WIFI_SSID "LGTeam1"

#define MQTT_SERVER "broker.mqttdashboard.com"
#define MQTT_SERVER_PORT 1883

#define WEB_SERVER "54.166.26.101"
#define WEB_SERVER_PORT 8000

#define DOOR 0
#define ALARM 1
#define LIGHT 2
#define PROXIMITY 3
#define STRING_TYPE 3
#define THERMOSTAT 4
#define HUMIDITY 5
#define MAX 6

#define OPEN 0
#define CLOSE 1
#define OCCUPIED 0
#define VACANT 1
#define OFF 0
#define ON 1

#define UPDATE_PERIODIC_TIME 1500
#define PROXIMITY_THRESHOLD 10
#define AUTOLIGHTOFFTIME (5 * 1000)
#define AUTOALARMONTIME (30 * 1000)
#define HEARTBEAT (10 * 1000) /* heartbeat */


/*********************************************************************************************
Function Definition
**********************************************************************************************/
void network_subscribeBUS(char* topic, byte* payload, unsigned int length);

/*********************************************************************************************
 Global and Local Variable Define
**********************************************************************************************/
dht DHT;                   // This sets up an equivalence between dht and DHT.
Servo myservo;
int ServoPin = 9;          // Servo Pin
int DoorSwitchPin = 2;     // Pin the door switch is connected to
int LightPin = 5;          // Pin that the corner light LED is connected to
int AlarmPin = 3;          // Pin that the alarm inidicator LED is connected to
int QtiPin = 6;            // The pin with QTI/proximity sensor
// Callback function header
char json[100];
WiFiClient wifiClient;
WiFiClient wifiHttpClient;
//PubSubClient client(MQTT_SERVER, 1883, callback, wifiClient);
PubSubClient client(MQTT_SERVER, MQTT_SERVER_PORT, network_subscribeBUS, wifiClient);

#define BUF_LEN          24
#define TOPIC_CLASS     "sanode"

#define NODE_NAME       nodeName

boolean boardConfig[] = { true, true, true, true, true, true };
char nodeName[BUF_LEN] ;

const char * names[] = { "door", "alarm", "light","proximity","thermostat","humidity" };
const char * values[][2] = { {"open","close"}, {"off","on"}, {"off","on"}, {"occupied","vacant"}};
const char * subscribe_method[] = { "control", "query" };
char topicPrefix[BUF_LEN]; 
unsigned long autoLightOffTime = AUTOLIGHTOFFTIME;
unsigned autoAlarmOnTime = AUTOALARMONTIME;
boolean  autoAlarmOnStart ;

int prevStatus[MAX];
int curStatus[MAX];

/*********************************************************************************************
  Implementation
**********************************************************************************************/

/*********************************************************************************************
  SA Protocol
**********************************************************************************************/


void protocol_getPayloadOfStatus(String name, String value, String &val) {
  val = "{";
  val+="\n";
  val+="\"";
  val+= name;
  val+="\":";
  val+="\"";
  val+= value;
  val+="\"";
  val+="\n";
  val+="}";
}
void protocol_getPayloadOfMessage (String type, String level, String message, String & payload){
  payload = "{";
  payload+="\n";
  payload+="\"type\":";
  payload+="\"";
  payload+=type;
  payload+="\",";
  payload+="\n";  
  payload+="\"";
  payload+=level;
  payload+="\":";
  payload+="\"";
  payload+=message;
  payload+="\"";  
  payload+="\n";
  payload+="}";
}
void protocol_makeTopic(String Method, String & topic)
{
	topic+=String(topicPrefix);
	topic+="/";
	topic+=Method;
}
void protocol_getMethodFromTopic(String topic, String & method)
{
  char * temp = strchr(topic.c_str()+strlen(topicPrefix),'/')+1;
  method = String(temp);
}

void protocol_getPayloadOfCreateNodeID(String & payload)
{
  payload = "{";
  payload+="\"nodeId\":";
  payload+="\"";
  payload+= NODE_NAME;
  payload+="\"";  
  payload+="}";
/* 
  "{\"nodeId\":\"0001\"}"
*/
}
/*********************************************************************************************
  SA Network
**********************************************************************************************/
void network_subscribeBUS(char* topic, byte* payload, unsigned int length) {
  String method;
  
  StaticJsonBuffer<200> jsonBuffer;

  // Allocate the correct amount of memory for the payload copy
  memcpy(json, payload, length);
  JsonObject& root = jsonBuffer.parseObject(json);
  protocol_getMethodFromTopic(String(topic),method);
  
  Serial.println(topic);

  if (strcmp("control", (char *)method.c_str()) == 0)
  {
    const char* name = root["name"];
    const char *value = root["value"];
    Serial.print("name:");   Serial.println(name);
    Serial.print("value:");   Serial.println(value);
    
    command_executeControl((char *)name,(char *)value);
  }
  else if (strcmp("query", (char *) method.c_str()) == 0)
      command_executeQuery();
}

void network_publishBUS(char * method, char * payload)
{
   String topic;
   protocol_makeTopic(String(method),topic);
   
   Serial.println(topic.c_str());
   Serial.println(payload);
   
   client.publish((char *)topic.c_str(),payload);
}
void network_publishBUS2(char * method, byte * payload, int len)
{
   String topic;
   protocol_makeTopic(String(method),topic);
   
   Serial.println(topic.c_str());
   
   client.publish((char *)topic.c_str(),payload,len);
}

byte network_postPage(char* domainBuffer,int thisPort,char* page,char* thisData)
{
  int  i, nretry = 10000, ret;
  int inChar;
  char outBuf[64];
  
  Serial.print(F("connecting..."));


  for ( i = 0 ; i < nretry ;i ++)
  {
    if ((ret = wifiHttpClient.connect(domainBuffer,thisPort)) == 1)
          break;
  }
  if(ret)
  {
    Serial.println(F("connected"));

    // send the header
    sprintf(outBuf,"POST %s HTTP/1.1",page);
    wifiHttpClient.println(outBuf);
    sprintf(outBuf,"Host: %s",domainBuffer);
    wifiHttpClient.println(outBuf);
    wifiHttpClient.println(F("Connection: close\r\nContent-Type: application/json\r\nx-client-id:75f9e675-9db4-4d02-b523-37521ef656ea"));
    sprintf(outBuf,"Content-Length: %u\r\n",strlen(thisData));
    wifiHttpClient.println(outBuf);

    // send the body (variables)
    wifiHttpClient.print(thisData);
  } 
  else
  {
    Serial.println(F("failed"));
    return 0;
  }
  
  int connectLoop = 0;

  while(wifiHttpClient.connected())
  {
    while(wifiHttpClient.available())
    {
      inChar = wifiHttpClient.read();
      Serial.write(inChar);
      connectLoop = 0;
    }

    delay(1);
    connectLoop++;
    if(connectLoop > 10000)
    {
      Serial.println();
      Serial.println(F("Timeout"));
      wifiHttpClient.stop();
    }
  }
  Serial.println();
  Serial.println(F("disconnecting."));
  wifiHttpClient.stop();
  return 1;
}

/*********************************************************************************************
  SA Command
**********************************************************************************************/
void command_sendMessage( String type, String level, String message)
{
     String payload; 
     protocol_getPayloadOfMessage(type, level,message,payload);
     network_publishBUS("notify",(char *)payload.c_str());
}

void command_sendStatus(int name_idx, int value)
{
     char buf[20];
     String strVal;
     String payload; 
     
     manager_getValueString(name_idx,value,strVal);
     protocol_getPayloadOfStatus(String(names[name_idx]), strVal,payload);
     network_publishBUS("status",(char *)payload.c_str());
}


void command_sendStatusAll(void)
{
    int i; 
    for ( i = MAX-1; i >= 0 ; i--)
    {
      if (!boardConfig[i])
            continue;
       command_sendStatus(i, curStatus[i]);
    }
}

void command_sendHeartBeat (void)
{
    network_publishBUS("heartbeat", "{}");
}

boolean executeQuery = true;

void command_executeQuery(void)
{
  executeQuery = true;
}

void command_postQuery(void)
{
  if (executeQuery)
  {
      command_sendStatusAll();
      executeQuery = false;
  }
}

boolean executeControl = false;

void command_postControl(void)
{
  if (executeControl)
  {
    manager_nodeStatusUpdate();
    executeControl = false;
  }
}


void command_executeControl (char * name, char * value)
{
   int name_idx = manager_getNameIdx(name);
   int ivalue = manager_getValueInt(name_idx,value);
   
   if (!boardConfig[name_idx])
       return ;

   if ( name_idx == DOOR)
       (ivalue==0) ? OpenDoor() : CloseDoor();
   else if (name_idx == LIGHT)
       (ivalue==0) ? LedOff(LightPin):LedOn(LightPin);
   else if (name_idx == ALARM)
   {
       CloseDoor();
       (ivalue==0) ? LedOff(AlarmPin):LedOn(AlarmPin);
   }

   if (curStatus[ALARM] == ON  && name_idx == DOOR && ivalue == OPEN)
       return ;
   
   if ( name_idx == ALARM)
       autoAlarmOnStart = false;
       
   curStatus[name_idx] = ivalue;
   executeControl = true;
}

void command_createSANode(void)
{
  String payload;
  protocol_getPayloadOfCreateNodeID(payload);
  network_postPage(WEB_SERVER,WEB_SERVER_PORT,"/session/createNode",(char *)payload.c_str());
}
/*********************************************************************************************
  SA Manager
**********************************************************************************************/

void managere_scanNode(void)
{
  if (manager_IsHouse())
      snprintf(nodeName,BUF_LEN,"%s","0001");
  else
  {
      snprintf(nodeName,BUF_LEN,"%s","0002");
      boardConfig[DOOR] = false;
      boardConfig[ALARM] = false;
      boardConfig[LIGHT] = false;
      boardConfig[PROXIMITY]  = true;
      boardConfig[THERMOSTAT] = false;
      boardConfig[HUMIDITY] = false;
  }
  
  Serial.print("nodeName"); Serial.println(nodeName);
  
}
boolean manager_IsHouse(void)
{    
      byte mac[6];
      static boolean bInit = false;
      static boolean bHouse = true;
      
     if (bInit == true)
        return bHouse; 
      
      WiFi.macAddress(mac);
     if (mac[5] == 0x78 && mac[4] == 0xC4 && mac[3] == 0x0E && mac[2] == 0x01 && mac[1] == 0x7f && mac[0] == 0xB3)
         bHouse = true;
     else
         bHouse = false;
     bInit = true;
     return bHouse ;
}


int manager_getNameIdx(char * name)
{
  int i; 
  for ( i = 0; i < sizeof(names)/sizeof(char *) ; i++)
  {
    if (strcmp(names[i],name) == 0)
    {
      return i;
      break ;
    }
  }
}

int manager_getValueInt(int name_idx, char * value)
{
  int i ; int ret_val ;
  if ( name_idx > STRING_TYPE)
  {
    sscanf(value,"%d", &ret_val);
    return ret_val;
  }
   if (strcmp(values[name_idx][0],value) == 0 )
       return 0;
   else 
       return 1;
}
void manager_getValueString(int name_idx, int value, String & sValue)
{
     char buf[BUF_LEN];
     String strVal;
      
     if (name_idx <=STRING_TYPE)
         sValue = String(values[name_idx][value]);
     else
     {
         sprintf(buf,"%d",value);
         sValue = String(buf);
     }
}

void manager_nodeStatusUpdate(void)
{
  int i ;
  for (i = 0 ; i < sizeof(names)/sizeof(char *) ;i ++)
  {
    if (boardConfig[i] == false)
      continue;
    
    if (curStatus[i] != prevStatus[i])
    {
      manager_statsTransitionPolicy(i,prevStatus[i],curStatus[i]);
      prevStatus[i] = curStatus[i];
      command_sendStatus(i, curStatus[i]);
    }
  }
}

void manager_nodeStatusCheck(unsigned long diff)
{
  int val;
  static unsigned long prev_msec =0;
  unsigned  long cur_msec ;
  
  cur_msec = millis();

  if ( (cur_msec - prev_msec) <= diff )
        return ;
  prev_msec = cur_msec;
  if (boardConfig[DOOR])
  {
    val = DoorState(DoorSwitchPin);
    curStatus[DOOR]=val;
  }
  if (boardConfig[PROXIMITY])
  {
    val  = ProximityVal(QtiPin); 
    val = (val > PROXIMITY_THRESHOLD) ? 1 : 0;
    curStatus[PROXIMITY]=val;
  }
  
  if (boardConfig[THERMOSTAT])
  {
    DHT.read11(DhtPin);
    val = DHT.temperature;
    curStatus[THERMOSTAT]=val;
  }
  
  if (boardConfig[HUMIDITY])
  { 
    val  = DHT.humidity;
    curStatus[HUMIDITY]=val;
  }
}

void manager_statsTransitionPolicy(int name_idx,int prev, int cur)
{
      if (manager_IsHouse() == false)
      {
        if (name_idx == PROXIMITY && prev == VACANT && cur == OCCUPIED)
            command_sendMessage("toast", "info","New mail is arrived");
        return;
      }     
      
     if (name_idx == DOOR && prev == CLOSE && cur == OPEN && curStatus[ALARM] == ON)
         command_sendMessage("toast", "warn", "The door is opened manually.");
     
     if (name_idx == PROXIMITY && prev == VACANT && cur == OCCUPIED && curStatus[ALARM] == ON)
         command_sendMessage("toast", "warn", "The house is suddenly occupied."); 
      

     if (name_idx == PROXIMITY && prev == OCCUPIED && cur == VACANT && curStatus[ALARM] == OFF)
     {
         command_sendMessage("alert", "info", "Door opened! But alarm is not ON.");
         autoAlarmOnStart = true;
     }
#if 0     
     if (name_idx == ALARM && prev == ON && cur == OFF && curStatus[PROXIMITY] == VACANT)
     {
         command_sendMessage("alert", "info", "Door opened! But alarm is not ON.");
         autoAlarmOnStart = true;
     }
#endif
}



/* 30sec */
void manager_statusPolicy(unsigned long diff)
{
  static unsigned long autoLightOffTimer = 0 ;
  static unsigned long prev_msec =0;
  unsigned long cur_msec ;
  unsigned long duration ;
  
  cur_msec = millis();
  duration = (cur_msec - prev_msec);

  if ( duration <= diff )
        return ;
  
   prev_msec = cur_msec;

/*  Auto Light Off */
  if (boardConfig[LIGHT] && boardConfig[PROXIMITY])
  {
    if (curStatus[PROXIMITY] == VACANT && curStatus[LIGHT] == ON)
          autoLightOffTimer += duration;
    else
          autoLightOffTimer = 0 ;
  
    if (autoLightOffTimer > autoLightOffTime)
    {      
        command_executeControl("light","off");
        autoLightOffTimer = 0 ;
    }
  }

  static unsigned long autoAlarmOnTimer = 0 ;
/*  Auto Lock  */  
  if (boardConfig[PROXIMITY] && boardConfig[DOOR])
  {
     if (curStatus[PROXIMITY] == VACANT && curStatus[ALARM] == OFF)
         autoAlarmOnTimer += duration;
     else
     {
         autoAlarmOnTimer = 0;
         autoAlarmOnStart = false;
     }
     
    if (autoAlarmOnTimer > autoAlarmOnTime)
    {
        if (autoAlarmOnStart)
        {      
          command_executeControl("alarm","on");
          command_executeControl("door","close");
          autoAlarmOnStart = false;
        }
        autoAlarmOnTimer = 0 ;
    }     
  }
  
  static unsigned long heartBeatTimer = 0;
  heartBeatTimer += duration;
  
  if (heartBeatTimer > HEARTBEAT)
  {
    heartBeatTimer = 0; 
    command_sendHeartBeat();
  }
}

void manager_initialization(void)
{
  static boolean init = false;
  if (init)
    return ;

    command_executeControl("alarm","off");
    command_executeControl("light","off");
    command_executeControl("door","close");
    
    snprintf(topicPrefix,sizeof(topicPrefix), "/%s/%s", TOPIC_CLASS, NODE_NAME);
    
    init = true;
}

/********************************************************************************************
Arduino Porting
*********************************************************************************************/


void setup() {
  int i =0 ;
  int nretry = 5;
  int result; 
  String payload;
  Serial.begin(115200);
  myservo.attach(ServoPin);                     // Attach to servo

  WiFi.begin(WIFI_SSID);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  // Print connection information to the debug terminal
  printConnectionStatus();
  managere_scanNode();
  
  Serial.print("webServer Connecting to ");
  command_createSANode();
  
  Serial.print("Connecting to ");
  Serial.println(MQTT_SERVER);
  
  for ( i = 0 ;i < 5 ; i ++)
  {
    result = client.connect(MQTT_CLIENT_ID);
   
    if (result) {
          Serial.println("Connected to MQTT broker");
          break;
    }
    else {
          Serial.println("MQTT connect failed");
          Serial.println("Will reset and try again...");
    }      
  }
  manager_initialization();
  
  for ( i = 0 ; i < sizeof(subscribe_method)/sizeof(char *) ; i ++)
  { 
    char topic[BUF_LEN];
    snprintf(topic, BUF_LEN, "%s/%s",topicPrefix, subscribe_method[i]);
    Serial.println(topic);
    
    if (!client.subscribe(topic)){
      Serial.println("fail to subscribe!");
    }
  }
}

void loop() {
  static boolean bInit = false;
  
  if (bInit == false)
  {
    manager_nodeStatusCheck(UPDATE_PERIODIC_TIME);
    command_postQuery();
    bInit=true;
  }
  
  client.loop();
  
  command_postQuery();
  command_postControl(); 

  //test();
  manager_nodeStatusCheck(UPDATE_PERIODIC_TIME);
  manager_nodeStatusUpdate();
  manager_statusPolicy(UPDATE_PERIODIC_TIME);
  
 //  if(!getPage(server,serverPort,pageAdd)) Serial.print(F("Fail "));
  //  else Serial.print(F("Pass "));
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
//  Serial.println( "Opening Door..." );
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
 
