# Adafruit-MQTT-and-Android
This project includes building a simple Android application using Android Studio and communicate with the Adafruit server.
<strong>Note</strong>: The clientID, subscriptionTopic, username, password are not provided due to privacy concerns. Therefore, in order to run the code properly, user must add the correspoding field into the code.
There should be 7 widgets in the application. Overview of functionalities of widgets:
<ul>
  <li><strong>Graph</strong>: Shows the graph over time of the data you sent to the server, given those data are all numbers and not weird strings. <strong>Note</strong>: In Android Studio's design view, you will only see the graph as a greyish area and that's perfectly normal.</li>
  <li><strong>0 and 1 buttons</strong>: Press 1 to send "1" to the server and 0 to send "0" to the server.</li>
  <li><strong>Textview field</strong>: Shows all sent data regardless of types.</li>
  <li><strong>Input Text Here EditText</strong>: Place where you enter the data to send to the server.</li>
  <li><strongLight Bulb</strong>: Shines when the data sent is 1 and turns off if the data is 0. The bulb stays the same if the data is not 0 or 1.
  <li><strong>Send button</strong>: Send the data to the server.</li>
</ul>
The application also support a UART reading from microbit feature. Simply use the Python code provided in the project and flash onto the microbit and you can use the buttons A and B to send 0s and 1s to the Android device. That data will automatically transported to the server as well.
