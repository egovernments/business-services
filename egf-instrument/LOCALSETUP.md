# Local Setup

To setup the egf-instrument service in your local system, clone the [Business services repository](https://github.com/egovernments/business-services).

## Dependencies


### Infra Dependency

- [X] Postgres DB
- [ ] Redis
- [ ] Elasticsearch
- [X] Kafka
  - [X] Consumer
  - [X] Producer

## Running Locally

To run the egf-instrument service locally, you need to port forward below services.

```bash
 kubectl port-forward -n egov {egf-master pod id} 8091:8080
``` 

Update below listed properties in **`application.properties`** before running the project:

```ini
-egov.services.egfmaster.hostname = {egf-master service hostname}
```
