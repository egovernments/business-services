# Local Setup

To setup the egov-hrms service in your local system, clone the [Core Service repository](https://github.com/egovernments/core-services).

## Dependencies

### Infra Dependency

- [x] Postgres DB
- [ ] Redis
- [ ] Elasticsearch
- [x] Kafka
  - [ ] Consumer
  - [x] Producer

## Running Locally

To run the egov-hrms services in local system, you need to port forward below services.

```bash
 kubectl port-forward -n egov {egov-idgen} 8087:8080
 kubectl port-forward -n egov {egov-mdms} 8088:8080
 kubectl port-forward -n egov {egov-user} 8089:8080
 kubectl port-forward -n egov {egov-filestore} 8090:8080
 kubectl port-forward -n egov {egov-localization} 8091:8080
``` 

Update below listed properties in `application.properties` before running the project:

```ini
 
-spring.datasource.url=jdbc:postgresql://localhost:5432/{local postgres db name}

-spring.flyway.url=jdbc:postgresql://localhost:5432/{local postgres db name}

-egov.mdms.host={mdms hostname}

-egov.user.host = {user service hostname}

-egov.idgen.host = {Id Gen service hostname}

-egov.filestore.host = {Filestore service hostname}

-egov.localization.host = {Localization service hostname}
```