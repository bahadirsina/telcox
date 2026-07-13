# TelcoX Local Kubernetes Skeleton

Bu klasör `INF-06` kapsamında Kind/Minikube için başlangıç Kubernetes iskeletidir.
Amaç production manifestleri yazmak değil; Docker Compose ile çalışan temel runtime yüzeylerini Kubernetes objelerine taşımaya başlamak.

## Kapsam

Bu iskelet şunları içerir:

- `telcox-local` namespace
- Ortak environment `ConfigMap`
- Local-only demo `Secret`
- Redis
- Kafka single-node local broker
- Discovery Server
- Config Server
- API Gateway
- BFF Service

İş mikroservisleri ve servis başına PostgreSQL StatefulSet/PVC manifestleri bilinçli olarak bu ilk iskelete eklenmedi. Onlar için sonraki adımda servis bazlı manifestler veya Helm/Kustomize overlays hazırlanabilir.

## Ön koşullar

Kind veya Minikube üzerinde image'ların lokal cluster içinde erişilebilir olması gerekir.

Örnek local image build:

```powershell
mvn clean package -DskipTests
docker compose build discovery-server config-server api-gateway bff-service
```

Kind kullanılıyorsa image load örneği:

```powershell
kind load docker-image telcox/discovery-server:1.0.0-SNAPSHOT
kind load docker-image telcox/config-server:1.0.0-SNAPSHOT
kind load docker-image telcox/api-gateway:1.0.0-SNAPSHOT
kind load docker-image telcox/bff-service:1.0.0-SNAPSHOT
```

Minikube kullanılıyorsa alternatif olarak Minikube Docker daemon içinde build alınabilir:

```powershell
minikube docker-env | Invoke-Expression
mvn clean package -DskipTests
docker compose build discovery-server config-server api-gateway bff-service
```

## Manifest kontrolü

```powershell
kubectl kustomize k8s/local
```

## Apply

```powershell
kubectl apply -k k8s/local
kubectl -n telcox-local get pods
```

## Local erişim

Gateway için port-forward:

```powershell
kubectl -n telcox-local port-forward svc/api-gateway 18080:8080
```

BFF için port-forward:

```powershell
kubectl -n telcox-local port-forward svc/bff-service 19011:9011
```

## Notlar

- `imagePullPolicy: IfNotPresent` kullanıldı; Kind/Minikube local image senaryosuna uygundur.
- Secret değerleri local demo içindir, production için uygun değildir.
- Kafka manifesti tek node local geliştirme içindir.
- BFF upstream URL'leri Kubernetes servis adlarına göre tanımlandı. İlgili backend servisleri deploy edilmediğinde dashboard endpoint upstream unavailable bilgisi döndürebilir.
