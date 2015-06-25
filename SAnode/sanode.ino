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
 Configuration
**********************************************************************************************/
//#define  DEBUG
#define LOCAL_SERVER
//#define HOTEL_WIFI
/*********************************************************************************************
 Define Macro and Enum
**********************************************************************************************/
#define DhtPin   8         // This #define defines the pin that will be used to get data 
                           // from the tempurature & humidity sensor.                            
//#define MQTT_SERVER "iot.eclipse.org"
#define MQTT_CLIENT_ID "SA_NODE01"

#ifdef HOTEL_WIFI
#define WIFI_SSID "Shadyside Inn"
#define WIFI_PASS "hotel5405"
#else
#define WIFI_SSID "LGTeam1"
#endif

#ifdef LOCAL_SERVER
#define MQTT_SERVER "54.166.26.101"
#else
#define MQTT_SERVER "broker.mqttdashboard.com"
#endif

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
#define AUTOLIGHTOFF 6
#define AUTOALARMON 7
#define MAX 8

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

#define NODE_NAME_LEN  5
#define BUF_LEN          24
#define PROFILENAME_LEN  16
#define TOPIC_CLASS     "sanode"
#define NODE_NAME        nodeName
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
WiFiClient wifiClient;
#define wifiHttpClient wifiClient
//WiFiClient wifiHttpClient;
//PubSubClient client(MQTT_SERVER, 1883, callback, wifiClient);
PubSubClient client(MQTT_SERVER, MQTT_SERVER_PORT, network_subscribeBUS, wifiClient);
boolean boardConfig[] = { true, true, true, true, true, true , true, true};
char nodeName[NODE_NAME_LEN] ;
char profileName[PROFILENAME_LEN];
const char * names[] = { "door", "alarm", "light","proximity","thermostat","humidity", "autolightoff", "autoalarmon"};
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
void protocol_getPayloadOfStatus(const char * name, const char * value, char * payload, int len) {
  snprintf(payload,len,"{\"%s\":\"%s\"}",name,value);  
}

void protocol_getPayloadOfProfileHead(const char * name, char * payload, int len) {
  snprintf(payload,len,"{\"profile\":\"%s\"}",name);
}

void protocol_getPayloadOfMessage (const char * type, const char * level, const char * message, char * payload, int len){
#ifdef DEBUG
  Serial.print("Free memory : ");
  Serial.println(get_free_memory());
#endif
  snprintf(payload,len,"{\"type\":\"%s\",\"%s\":\"%s\"}",type,level,message);
}
void protocol_makeTopic(const char * method, char * topic, int len)
{
  snprintf(topic,len,"%s/%s",topicPrefix,method);
}
void protocol_getMethodFromTopic(const char * topic, char * method, int len)
{
  char * temp = (char *)topic + strlen(topicPrefix) + 1; 
  strncpy(method,temp,len);
}
void protocol_getPayloadOfCreateNodeID(char * payload, int len)
{
  snprintf(payload,len,"{\"nodeId\":\"%s\"}", NODE_NAME);
}
/*********************************************************************************************
  SA Network
**********************************************************************************************/
#define METHOD_BUF  16
#define TOPIC_LEN  32
#define JSON_BUF  48

char json[JSON_BUF];
void network_subscribeBUS(char* topic, byte* payload, unsigned int length) {
  char method[METHOD_BUF];
  StaticJsonBuffer<JSON_BUF> jsonBuffer;
  // Allocate the correct amount of memory for the payload copy
#ifdef DEBUG
  Serial.print("Free memory : ");
  Serial.println(get_free_memory());
#endif
  if (length > sizeof(json))
      Serial.println("json buffer overflow");    

  memcpy(json, payload, length);
  JsonObject& root = jsonBuffer.parseObject(json);
  
  protocol_getMethodFromTopic(topic,method,sizeof(method));
  
  Serial.println(topic);

  if (strcmp("control", (char *)method) == 0)
  {
    const char* name = root["name"];
    const char *value = root["value"];
    Serial.print("name:");   Serial.println(name);
    Serial.print("value:");   Serial.println(value);
    
    command_executeControl((char *)name,(char *)value);
  }
  else if (strcmp("query", (char *) method) == 0)
      command_executeQuery();
}

void network_publishBUS(char * method, char * payload)
{
   char topic[TOPIC_LEN];
   protocol_makeTopic(method,topic, sizeof(topic));
   
   Serial.println(topic);
   Serial.println(payload);
   
   client.publish(topic,payload);
}

byte network_postPage(char* domainBuffer,int thisPort,char* page,char* thisData)
{
  int  i, nretry = 10000, ret;
  int inChar;
  char outBuf[64];
  
#ifdef DEBUG
  Serial.print("Free memory : ");
  Serial.println(get_free_memory());
#endif
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
    snprintf(outBuf,sizeof(outBuf),"POST %s HTTP/1.1",page);
    wifiHttpClient.println(outBuf);
    snprintf(outBuf,sizeof(outBuf),"Host: %s",domainBuffer);
    wifiHttpClient.println(outBuf);
    wifiHttpClient.println(F("Connection: close\r\nContent-Type: application/json\r\nx-client-id:75f9e675-9db4-4d02-b523-37521ef656ea"));
    snprintf(outBuf,sizeof(outBuf),"Content-Length: %u\r\n",strlen(thisData));
    wifiHttpClient.println(outBuf);
    // send the body (variables)
    wifiHttpClient.print(thisData);
  } 
  else
  {
    Serial.println(F("failed"));
    return 0;
  }
  
  delay(10);
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
#define CLIENTID_LEN  16
void network_eventBusConnect(boolean reConnect)
{
  int i =0 ;
  int nretry = 5;
  int result; 
  char clientID[CLIENTID_LEN];
  
  snprintf(clientID,sizeof(clientID),"%s_xySAnode", NODE_NAME);
  
  if (reConnect)
    client.disconnect();
  
  Serial.println(clientID);
  Serial.println(MQTT_SERVER);
  for ( i = 0 ;i < 5 ; i ++)
  {
    result = client.connect(clientID);
   
    if (result) {
          Serial.println("Connected to MQTT broker");
          break;
    }
    else {
          Serial.println("MQTT connect failed");
          Serial.println("Will reset and try again...");
    }      
  }
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
/*********************************************************************************************
  SA Command
**********************************************************************************************/
#define VALUE_LEN  16
#define COMMAN_BUF_SIZE  128
#define PAYLOAD_LEN      128

void command_sendProfileHead(void)
{
    char payload[PAYLOAD_LEN];   
    protocol_getPayloadOfProfileHead(profileName,payload,sizeof(payload));
    network_publishBUS("profile",payload);
}

void command_sendMessage( const char * type, const char * level, char * message)
{
     char payload[PAYLOAD_LEN];
     protocol_getPayloadOfMessage(type, level,message,payload,sizeof(payload));
     network_publishBUS("notify",payload);
}

void command_sendStatus(int name_idx, int value)
{
     char cVal[VALUE_LEN]; 
     char payload[PAYLOAD_LEN];
     manager_getValueString(name_idx,value,cVal,sizeof(cVal));
     protocol_getPayloadOfStatus(names[name_idx], cVal,payload, sizeof(payload));
     network_publishBUS("status",payload);
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
    network_publishBUS("heartbeat", "{\"\"}");
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
   
   if ( ivalue == -1 )
       return; 
   
   if (!boardConfig[name_idx])
       return ;
       
   if (curStatus[ALARM] == ON  && name_idx == DOOR && ivalue == OPEN)
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
   else if (name_idx == AUTOLIGHTOFF)
     autoLightOffTime = ivalue;
   else if (name_idx == AUTOALARMON)
     autoAlarmOnTime = ivalue;
   
   if ( name_idx == ALARM)
       autoAlarmOnStart = false;
       
   curStatus[name_idx] = ivalue;
   executeControl = true;
}
void command_createSANode(void)
{
  char payload[PAYLOAD_LEN];
  protocol_getPayloadOfCreateNodeID(payload, sizeof(payload));
  Serial.println(payload);
  network_postPage(WEB_SERVER,WEB_SERVER_PORT,"/session/createNode",payload);
}
/*********************************************************************************************
  SA Manager
**********************************************************************************************/

void manager_scanNode(void)
{
  if (manager_IsHouse())
  {
      snprintf(nodeName,sizeof(nodeName),"%s","0001");
      snprintf(profileName,sizeof(profileName),"%s","house");
  }
  else
  {
      snprintf(nodeName,sizeof(nodeName),"%s","0002");
      snprintf(profileName,sizeof(profileName),"%s","mailbox");
      memset(boardConfig, 0x00, sizeof(boardConfig));      
      boardConfig[PROXIMITY]  = true;
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
   else if (strcmp(values[name_idx][1],value) == 0 )
       return 1;
   else
       return -1;
}

void manager_getValueString(int name_idx, int value, char * cVal, int len)
{      
     if (name_idx <=STRING_TYPE)
         strncpy(cVal,values[name_idx][value],len);
     else
         snprintf(cVal,len,"%d",value);
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
  if (boardConfig[AUTOLIGHTOFF])
  { 
    val  = autoLightOffTime;
    curStatus[AUTOLIGHTOFF]=val;
  }
  if (boardConfig[AUTOALARMON])
  { 
    val  = autoAlarmOnTime;
    curStatus[AUTOALARMON]=val;
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
         command_sendMessage("toast", "warn", "Door is opened manually");
     
     if (name_idx == PROXIMITY && prev == VACANT && cur == OCCUPIED && curStatus[ALARM] == ON)
         command_sendMessage("toast", "warn", "House is suddenly occupied"); 
      

     if (name_idx == PROXIMITY && prev == OCCUPIED && cur == VACANT && curStatus[ALARM] == OFF)
     {
         command_sendMessage("alert", "info", "House is vacant, but alarm is off");
         autoAlarmOnStart = true;
     }
}
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
#ifdef DEBUG
void showMemoryMap(void)
{
  char stack = 1;
  extern char *__data_start;
  extern char *__data_end;
  extern char *__bss_start;
  extern char *__bss_end;
  extern char *__heap_start;
  extern char *__heap_end;
  
  int	data_size	=	(int)&__data_end - (int)&__data_start;
  int	bss_size	=	(int)&__bss_end - (int)&__data_end;
  int	heap_end	=	(int)&stack - (int)&__malloc_margin;
  int	heap_size	=	heap_end - (int)&__bss_end;
  int	stack_size	=	RAMEND - (int)&stack + 1;
  int	Aavailable	=	(RAMEND - (int)&__data_start + 1);
  
  Aavailable	-=	data_size + bss_size + heap_size + stack_size;

  Serial.print("available =");
  Serial.println(Aavailable);
  Serial.println();
  Serial.println();
}
#endif

int get_free_memory()
{
extern char __bss_end;
extern char *__brkval;

  int free_memory;

  if((int)__brkval == 0)
    free_memory = ((int)&free_memory) - ((int)&__bss_end);
  else
    free_memory = ((int)&free_memory) - ((int)__brkval);

  return free_memory;
}

void setup() {
  Serial.begin(115200);
  myservo.attach(ServoPin);                     // Attach to servo

#ifdef HOTEL_WIFI
  WiFi.begin(WIFI_SSID, WIFI_PASS);
#else
  WiFi.begin(WIFI_SSID);
#endif  
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  // Print connection information to the debug terminal
  printConnectionStatus();
  Serial.println();
  Serial.print("Free memory : ");
  Serial.println(get_free_memory());

  manager_scanNode();
  
  Serial.print("webServer Connecting to ");
  command_createSANode();
  
  manager_initialization();
  
  network_eventBusConnect(false);
}

void loop() {
  static boolean bInit = false;
  if (bInit == false)
  {
    manager_nodeStatusCheck(UPDATE_PERIODIC_TIME);
    command_postQuery();
   // command_sendProfileHead();
    bInit=true;
  }
  if (client.loop() == 0)
  {
    Serial.println("Server Reconnect");
    network_eventBusConnect(true);
  }
  command_postQuery();
  command_postControl(); 
  manager_nodeStatusCheck(UPDATE_PERIODIC_TIME);
  manager_nodeStatusUpdate();
  manager_statusPolicy(UPDATE_PERIODIC_TIME);
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
 
