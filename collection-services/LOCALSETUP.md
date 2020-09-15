# Local Setup

To setup the billing-service in your local system, clone the git repo(https://github.com/egovernments/business-services).

## Dependencies


### Infra Dependency

- [X] Postgres DB
- [ ] Redis
- [ ] Elasticsearch
- [X] Kafka
  - [X] Consumer
  - [X] Producer

## Running Locally

To run the service locally, you need to port forward below services.

```bash

 - User-service
 - egf-master
 - common-master
 - instrument-service
 - billing-service
 - IdGen
 - Mdms
 - Apportion-service
 
 kubectl port-forward -n egov {pod id} 8080:8080
 
``` 

Update below listed properties in **`application.properties`** before running the project:

```ini

-egov.idgen.hostname = 
-egov.services.billing_service.hostname=
-user.service.hostname=
-egov.apportion.host=
-egov.mdms.host=
-coll.notification.ui.host=
-egov.egfmasters.hostname=
-egov.egfcommonmasters.hostname =

```
