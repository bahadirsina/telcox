# TelcoX Local Kubernetes Manifests

Bu klasör `INF-06` kapsamında Kind/Minikube için local Kubernetes manifestlerini içerir.
Amaç production-grade cluster tasarımı değil; Docker Compose ile çalışan TelcoX platformunu eğitim/demo ortamında Kubernetes üzerinde ayağa kaldırılabilir hale getirmektir.

## Kapsam

Manifest seti şunları içerir:

- `telcox-local` namespace
- Ortak environment `ConfigMap`
- Local-only demo `Secret`
- Redis
- Kafka
- Kafka Connect
- Keycloak ve Keycloak PostgreSQL
- Servis başına PostgreSQL instance'ları
- Discovery Server
- Config Server
- API Gateway
- BFF Service
- Identity, Customer, Product Catalog, Order, Subscription, Usage, Billing, Payment, Notification ve Ticket servisleri
- MailHog
- Zipkin
- Prometheus
- Grafana
- Kafka UI
- pgAdmin
- Signal Atlas frontend

PostgreSQL manifestleri local demo için `emptyDir` kullanır. Pod silindiğinde veri kalıcı değildir. Production ortam için StatefulSet, PVC, secret yönetimi ve backup stratejisi ayrıca tasarlanmalıdır.

## Ön koşullar

Minikube veya Kind cluster çalışıyor olmalıdır.

Minikube örneği:

```powershell
minikube start --driver=docker --memory=8192 --cpus=4
```

> Daha önce oluşturulmuş bir Minikube profili varsa CPU/memory değerleri değişmeyebilir. Bu durumda mevcut profil ile devam edilebilir veya bilinçli olarak `minikube delete` sonrası yeniden oluşturulabilir.

## Build ve image load

Local cluster'ın TelcoX image'larına erişebilmesi gerekir.

```powershell
mvn -B -ntp package -DskipTests
docker compose build discovery-server config-server api-gateway bff-service identity-service customer-service product-catalog-service order-service subscription-service usage-service billing-service payment-service notification-service ticket-service signal-atlas-web
```

Minikube için örnek:

```powershell
minikube image load telcox/discovery-server:1.0.0-SNAPSHOT
minikube image load telcox/config-server:1.0.0-SNAPSHOT
minikube image load telcox/api-gateway:1.0.0-SNAPSHOT
minikube image load telcox/bff-service:1.0.0-SNAPSHOT
minikube image load telcox/identity-service:1.0.0-SNAPSHOT
minikube image load telcox/customer-service:1.0.0-SNAPSHOT
minikube image load telcox/product-catalog-service:1.0.0-SNAPSHOT
minikube image load telcox/order-service:1.0.0-SNAPSHOT
minikube image load telcox/subscription-service:1.0.0-SNAPSHOT
minikube image load telcox/usage-service:1.0.0-SNAPSHOT
minikube image load telcox/billing-service:1.0.0-SNAPSHOT
minikube image load telcox/payment-service:1.0.0-SNAPSHOT
minikube image load telcox/notification-service:1.0.0-SNAPSHOT
minikube image load telcox/ticket-service:1.0.0-SNAPSHOT
minikube image load telcox/signal-atlas-web:0.1.0
```

## Manifest kontrolü

```powershell
kubectl kustomize k8s/local
```

## Deploy

```powershell
kubectl apply -k k8s/local
kubectl -n telcox-local get pods
```

Tüm local stack için önerilen yöntem aşamalı script kullanmaktır. Script image build/load sonrasında manifestleri dependency sırasına göre uygular ve rollout bekler:

```bash
scripts/local-k8s-deploy.sh
```

`kubectl apply -k k8s/local` manifest setini tek seferde uygular. Bu komut schema/render doğrulaması için kullanışlıdır; ancak tüm stack'i tek dalgada başlatmak local Minikube üzerinde API server ve kubelet'i gereğinden fazla zorlayabilir.

## Local erişim

Discovery Server:

```powershell
kubectl -n telcox-local port-forward svc/discovery-server 18761:8761
```

API Gateway:

```powershell
kubectl -n telcox-local port-forward svc/api-gateway 18080:8080
```

BFF:

```powershell
kubectl -n telcox-local port-forward svc/bff-service 19011:9011
```

Frontend:

```powershell
kubectl -n telcox-local port-forward svc/signal-atlas-web 15173:80
```

Keycloak:

```powershell
kubectl -n telcox-local port-forward svc/keycloak 18083:8080
```

## Notlar

- `imagePullPolicy: IfNotPresent` local image senaryosuna uygundur.
- Secret değerleri yalnızca local demo içindir.
- PostgreSQL verileri local demo için kalıcı değildir.
- Kafka tek node local geliştirme topolojisidir.
- Keycloak local manifesti servis olarak ayağa kalkar; realm import/production hostname ayarları daha ileri ortamlar için ayrıca sertleştirilmelidir.
