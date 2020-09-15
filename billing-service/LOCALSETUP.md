# Local Setup

To setup the billing-service in your local system, clone the git repo(https://github.com/egovernments/business-services).

## Dependencies


### Infra Dependency

- [X] Postgres DB
- [ ] Redis
- [ ] Elasticsearch
- [X] Kafka
  - [X] Consumer
  - [ ] Producer

## Running Locally

To run the service locally, you need to port forward below services.

```bash

 - User-service
 - IdGen
 - Mdms
 - Apportion-service
 
 kubectl port-forward -n egov {pod id} 8080:8080
 
``` 

Update below listed properties in **`application.properties`** before running the project:

```ini

-egov.idgen.hostname = 
-user.service.hostname=
-egov.localization.host=
-egov.apportion.host=

```
