#!/usr/bin/env bash
# Ejecutar dentro de EC2: sudo bash install-backend.sh ENDPOINT_RDS ARCHIVO_JAR
set -euo pipefail
if [[ $EUID -ne 0 || $# -ne 2 ]]; then
  echo 'Uso: sudo bash install-backend.sh ENDPOINT_RDS ARCHIVO_JAR' >&2
  exit 1
fi
DB_HOST=$1
JAR_PATH=$2
if [[ ! $DB_HOST =~ ^[a-zA-Z0-9.-]+\.rds\.amazonaws\.com$ || ! -f $JAR_PATH ]]; then
  echo 'Endpoint RDS o archivo JAR no valido.' >&2
  exit 1
fi
cloud-init status --wait
java -version
read -r -s -p 'Clave MySQL que ingresaste en CloudFormation: ' DB_PASSWORD
printf '\n'
if [[ ! $DB_PASSWORD =~ ^[a-zA-Z0-9]{20,64}$ ]]; then
  echo 'La clave debe cumplir el formato de la plantilla: 20 a 64 letras y numeros.' >&2
  exit 1
fi
id anonymus >/dev/null 2>&1 || useradd --system --shell /sbin/nologin anonymus
install -d -m 755 /opt/anonymus
install -d -m 700 /etc/anonymus
install -m 644 "$JAR_PATH" /opt/anonymus/backend.jar.new
mv /opt/anonymus/backend.jar.new /opt/anonymus/backend.jar
export DB_HOST DB_PASSWORD
python3 <<'PY'
import json
import os
import secrets
from pathlib import Path

path = Path('/etc/anonymus/backend.env')
# Conservar el secreto JWT al reinstalar para no invalidar sesiones sin necesidad.
jwt = secrets.token_hex(48)
if path.exists():
    for line in path.read_text().splitlines():
        if line.startswith('JWT_SECRET='):
            jwt = json.loads(line.split('=', 1)[1])
values = {
    'SPRING_PROFILES_ACTIVE': 'aws',
    'DB_URL': 'jdbc:mysql://' + os.environ['DB_HOST'] + ':3306/tecnoshop?sslMode=VERIFY_IDENTITY',
    'DB_USER': 'anonymusadmin',
    'DB_PASSWORD': os.environ['DB_PASSWORD'],
    'JWT_SECRET': jwt,
}
os.umask(0o077)
path.write_text(''.join(key + '=' + json.dumps(value) + '\n' for key, value in values.items()))
path.chmod(0o600)
PY
unset DB_PASSWORD
# Certificados oficiales para verificar identidad y cifrado de RDS.
curl --fail --silent --show-error --location --retry 3 \
  https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem \
  -o /opt/anonymus/rds-bundle.pem
python3 <<'PY'
import re
import subprocess
from pathlib import Path
bundle = Path('/opt/anonymus/rds-bundle.pem').read_text()
certs = re.findall(r'-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----', bundle, re.S)
if not certs:
    raise SystemExit('No se encontraron certificados RDS')
store = Path('/opt/anonymus/rds-truststore.new')
store.unlink(missing_ok=True)
for i, cert in enumerate(certs):
    subprocess.run(['keytool', '-importcert', '-noprompt', '-alias', f'rds-{i}',
                    '-keystore', str(store), '-storetype', 'PKCS12', '-storepass', 'changeit'],
                   input=cert, text=True, check=True, stdout=subprocess.DEVNULL,
                   stderr=subprocess.PIPE)
store.chmod(0o644)
store.replace('/opt/anonymus/rds-truststore')
PY
cat > /etc/systemd/system/anonymus.service <<'UNIT'
[Unit]
Description=Anonymus Spring Boot
After=network-online.target
Wants=network-online.target

[Service]
User=anonymus
Group=anonymus
EnvironmentFile=/etc/anonymus/backend.env
WorkingDirectory=/opt/anonymus
ExecStart=/usr/bin/java -Xms128m -Xmx384m -Djavax.net.ssl.trustStore=/opt/anonymus/rds-truststore -Djavax.net.ssl.trustStorePassword=changeit -jar /opt/anonymus/backend.jar
Restart=on-failure
RestartSec=10
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
UMask=0077

[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable anonymus
systemctl restart anonymus
for attempt in {1..60}; do
  if curl --fail --silent http://127.0.0.1:8080/api/v1/categorias >/dev/null; then
    echo 'API disponible: GET /api/v1/categorias respondio correctamente.'
    exit 0
  fi
  sleep 2
done
echo 'La API no respondio. Revisar: sudo journalctl -u anonymus -n 100 --no-pager' >&2
exit 1
