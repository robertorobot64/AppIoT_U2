#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <time.h>
#include <Preferences.h>

const char* ssid     = "Redmi Note 13 5G";   // debe coincidir EXACTO con tu hotspot (2,4 GHz)
const char* password = "MartinL1";
const char* firebaseURL = "https://appiotu2-default-rtdb.firebaseio.com"; // sin / final

const int PIN_TRIG = 14, PIN_ECHO = 32, PIN_LED = 27;
const unsigned long INTERVALO_MEDICION = 5000;   // muestreo cada 5 s (energía)
const unsigned long INTERVALO_COMANDOS = 2000;   // sondeo de órdenes cada 2 s
unsigned long UMBRAL_CM = 10;

Preferences prefs;                  // buffer offline en memoria no volátil
unsigned long tMed = 0, tCmd = 0;
long ultimaDistancia = 0;
bool wifiListo = false;

void conectarWiFi() {               // reconexión automática
  if (WiFi.status() == WL_CONNECTED) { wifiListo = true; return; }
  wifiListo = false;
  WiFi.begin(ssid, password);
  int i = 0;
  while (WiFi.status() != WL_CONNECTED && i < 20) { delay(500); i++; }
  wifiListo = (WiFi.status() == WL_CONNECTED);
  if (wifiListo) {
    configTime(-3 * 3600, 0, "pool.ntp.org");
    Serial.printf("[WiFi] OK IP=%s\n", WiFi.localIP().toString().c_str());
  } else {
    Serial.println("[WiFi] SIN RED -> buffer local activo");
  }
}

String fechaActual() {
  struct tm t;
  if (!getLocalTime(&t)) return "01-01-2026";
  char b[11]; strftime(b, 11, "%d-%m-%Y", &t); return String(b);
}
String horaActual() {
  struct tm t;
  if (!getLocalTime(&t)) return "00:00:00";
  char b[9]; strftime(b, 9, "%H:%M:%S", &t); return String(b);
}

bool firebasePUT(String path, String json) {
  if (!wifiListo) return false;
  HTTPClient http;
  http.begin(String(firebaseURL) + path + ".json");
  http.addHeader("Content-Type", "application/json");
  int c = http.PUT(json);
  http.end();
  Serial.printf("[TX] PUT %s -> %d\n", path.c_str(), c);
  return c == 200;
}

String firebaseGET(String path) {
  if (!wifiListo) return "";
  HTTPClient http;
  http.begin(String(firebaseURL) + path + ".json");
  int c = http.GET();
  String out = (c == 200) ? http.getString() : "";
  http.end();
  return out;
}

long medirDistancia() {
  digitalWrite(PIN_TRIG, LOW);  delayMicroseconds(2);
  digitalWrite(PIN_TRIG, HIGH); delayMicroseconds(10);
  digitalWrite(PIN_TRIG, LOW);
  return pulseIn(PIN_ECHO, HIGH, 30000) * 0.0343 / 2;  // timeout 30 ms
}

void guardarOffline(String key, String json) {
  String buf = prefs.getString("buffer", "");
  if (buf.length() > 3000) buf = buf.substring(buf.indexOf('|') + 1);
  prefs.putString("buffer", buf + key + "," + json + "|");
  Serial.println("[BUF] medición guardada localmente");
}

void flushOffline() {
  String buf = prefs.getString("buffer", "");
  if (buf.length() == 0) return;
  while (buf.length() > 0) {
    int sep = buf.indexOf('|');
    String reg = buf.substring(0, sep);
    buf = buf.substring(sep + 1);
    int c = reg.indexOf(',');
    String key = reg.substring(0, c);
    String json = reg.substring(c + 1);
    if (!firebasePUT("/Datos/" + key, json)) { buf = reg + "|" + buf; break; }
  }
  prefs.putString("buffer", buf);
  Serial.println("[BUF] flush completado");
}

void cicloMedicion() {
  long d = medirDistancia();
  ultimaDistancia = d;   // CORRECCIÓN 1: antes "= 0" mataba el enclavamiento
  String desc = (d > 0 && d <= UMBRAL_CM) ? "Obstáculo Detectado" : (d == 0 ? "Fuera de rango" : "Sin Obstáculos");
  String key  = "m" + String(millis());
  String json = "{\"descripcion\":\"" + desc + "\",\"distancia\":" + String(d) +
                ",\"fecha\":\"" + fechaActual() + "\",\"hora\":\"" + horaActual() + "\"}";
  Serial.printf("[SENSOR] %ld cm -> %s\n", d, desc.c_str());
  if (wifiListo) {
    if (!firebasePUT("/Datos/" + key, json)) guardarOffline(key, json);
    else flushOffline();
  } else {
    guardarOffline(key, json);
  }
}

void revisarComandos() {
  String payload = firebaseGET("/Comandos");
  if (payload.length() == 0 || payload == "null") return;
  JsonDocument doc;
  if (deserializeJson(doc, payload)) return;
  for (JsonPair kv : doc.as<JsonObject>()) {
    JsonObject cmd = kv.value().as<JsonObject>();
    const char* estado = cmd["estado"] | "";
    if (String(estado) == "pendiente") {
      String tipo  = cmd["tipo"]  | "";
      String valor = cmd["valor"] | "";
      bool ejecutado = true;
      String motivo = "";
      if (tipo == "led") {
        if (valor == "on" && ultimaDistancia > 0 && ultimaDistancia <= UMBRAL_CM) {
          digitalWrite(PIN_LED, LOW);          // enclavamiento de seguridad
          ejecutado = false;
          motivo = "obstaculo en rango";
          Serial.println("[CMD] led=on RECHAZADO: obstáculo dentro del umbral");
        } else {
          digitalWrite(PIN_LED, valor == "on" ? HIGH : LOW);
        }
      } else if (tipo == "umbral") {
        UMBRAL_CM = valor.toInt();
      } else if (tipo == "medir") {
        cicloMedicion();
      }
      Serial.printf("[CMD] %s=%s %s\n", tipo.c_str(), valor.c_str(), ejecutado ? "EJECUTADO" : "RECHAZADO");
      firebasePUT("/Comandos/" + String(kv.key().c_str()),
        "{\"tipo\":\"" + tipo + "\",\"valor\":\"" + valor +
        "\",\"estado\":\"" + String(ejecutado ? "ejecutado" : "rechazado") +
        "\",\"motivo\":\"" + motivo +
        "\",\"fechaEjec\":\"" + fechaActual() + "\",\"horaEjec\":\"" + horaActual() + "\"}");
    }   // CORRECCIÓN 2: estas tres llaves faltaban en tu versión
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(PIN_TRIG, OUTPUT);
  pinMode(PIN_ECHO, INPUT);
  pinMode(PIN_LED, OUTPUT);
  prefs.begin("appiot", false);
  conectarWiFi();
}

void loop() {
  conectarWiFi();
  if (millis() - tCmd >= INTERVALO_COMANDOS) { tCmd = millis(); revisarComandos(); }
  if (millis() - tMed >= INTERVALO_MEDICION) { tMed = millis(); cicloMedicion(); }
  delay(100);
}