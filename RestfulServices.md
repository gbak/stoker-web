# Stoker-web RESTful interface #

## URLs for collecting data ##

### Get Device Data ###
http://192.168.5.5/api/v1/devices
```
{"devices":[{"type":"pit","id":"E70000116F279030","cooker":"Egg","targetTemp":"30","alarmType":"ALARM_FIRE","currentTemp":"0","blower":{"type":"fan","id":"230000002A55C305","cooker":null,"fanOn":false,"totalRuntime":0,"name":"Blower 1"},"name":"pit sensor","alarmLow":"325","alarmHigh":"400"},{"type":"probe","id":"DB0000116F0BEC30","cooker":"Egg","targetTemp":"155","alarmType":"ALARM_FOOD","currentTemp":"0","name":"Turkey","alarmLow":"2000","alarmHigh":"3000000"}],"receivedDate":null,"logCount":{"logItemCount":{"Egg":1}}}
```

### Get Device Data for Specific device ###
http://192.168.5.5/api/v1/devices/{id}

http://192.168.5.5/api/v1/devices/E70000116F279030
```
{"devices":[{"type":"pit","id":"E70000116F279030","cooker":"Egg","targetTemp":"30","alarmType":"ALARM_FIRE","currentTemp":"0","blower":{"type":"fan","id":"230000002A55C305","cooker":null,"fanOn":false,"totalRuntime":0,"name":"Blower 1"},"name":"pit sensor","alarmLow":"325","alarmHigh":"400"}],"receivedDate":null,"logCount":{"logItemCount":{"Egg":1}}}
```

### Get data in Cooker grouped format ###
http://192.168.5.5/api/v1/cookers
```
[{"name":"Egg","pitProbe":{"type":"pit","id":"E70000116F279030","cooker":null,"targetTemp":"30","alarmType":"ALARM_FIRE","currentTemp":"48.2","blower":{"type":"fan","id":"230000002A55C305","cooker":null,"fanOn":false,"totalRuntime":0,"name":"Blower 1"},"name":"pit sensor","alarmLow":"325","alarmHigh":"400"},"probeList":[{"type":"Probe","id":"DB0000116F0BEC30","cooker":null,"targetTemp":"155","alarmType":"ALARM_FOOD","currentTemp":"60.6","name":"Turkey","alarmLow":"2000","alarmHigh":"3000000"}]}]
```

### Get Logs for all Cookers ###
http://192.168.5.5/api/v1/logs/cooker
```
{"logList":[{"logName":"Default_Egg","cookerName":"Egg","startDate":1356483269270,"deviceList":[{"type":"pit","id":"E70000116F279030","cooker":"Egg","targetTemp":"30","alarmType":"ALARM_FIRE","currentTemp":"48.2","blower":{"type":"fan","id":"230000002A55C305","cooker":null,"fanOn":false,"totalRuntime":0,"name":"Blower 1"},"name":"pit sensor","alarmLow":"325","alarmHigh":"400"},{"type":"probe","id":"DB0000116F0BEC30","cooker":"Egg","targetTemp":"155","alarmType":"ALARM_FOOD","currentTemp":"60.6","name":"Turkey","alarmLow":"2000","alarmHigh":"3000000"}]}],"receivedDate":null}
```

### Get Logs for specific Cooker ###
http://192.168.5.5/api/v1/logs/cooker/{cooker}

http://192.168.5.5/api/v1/logs/cooker/Egg
```
{"logList":[{"logName":"Default_Egg","cookerName":"Egg","startDate":1356483269270,"deviceList":[{"type":"pit","id":"E70000116F279030","cooker":"Egg","targetTemp":"30","alarmType":"ALARM_FIRE","currentTemp":"48.2","blower":{"type":"fan","id":"230000002A55C305","cooker":null,"fanOn":false,"totalRuntime":0,"name":"Blower 1"},"name":"pit sensor","alarmLow":"325","alarmHigh":"400"},{"type":"probe","id":"DB0000116F0BEC30","cooker":"Egg","targetTemp":"155","alarmType":"ALARM_FOOD","currentTemp":"60.6","name":"Turkey","alarmLow":"2000","alarmHigh":"3000000"}]}],"receivedDate":null}
```

### Count of logs for each available cooker ###
http://192.168.5.5/api/v1/logs/count
```
{"logItemCount":{"Egg":1}}
```

Details for each of these calls can be found in the RestServices.java soruce file.  The data types can be found in com.gbak.sweb.common.json.

There is a built target called 'common' that can be used to build a stokerweb-common.jar file.  This file can be used to use these data classes in other projects.