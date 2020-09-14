# Local Setup

To setup the finance-collections-voucher-consumer service in your local system, clone the [Business services repository](https://github.com/egovernments/business-services).

## Dependencies


### Infra Dependency

- [X] Postgres DB
- [ ] Redis
- [ ] Elasticsearch
- [X] Kafka
  - [X] Consumer
  - [ ] Producer

## Running Locally

To run the finance-collections-voucher-consumer service locally, you need to port forward below services.

```bash
 kubectl port-forward -n egov {egov-mdms-service pod id} 8087:8080
 kubectl port-forward -n egov {egov-user pod id} 8088:8080
 kubectl port-forward -n egov {egf-instrument pod id} 8089:8080
 kubectl port-forward -n egov {collection-service pod id} 8090:8080
 kubectl port-forward -n egov {egf-master pod id} 8091:8080
``` 

Update below listed properties in **`application.properties`** before running the project:

```ini
-egov.services.egov.user.host = {egov-user service hostname}
-egov.services.mdms.hostname = {egov-mdms-service service hostname}
-egov.services.egfinstrument.hostname = {egf-instrument service hostname}
-egov.services.collections.hostname = {collection-service service hostname}
-egov.services.egfmaster.hostname = {egf-master service hostname}
```
