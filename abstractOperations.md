# Implemented abstract operations:
* CREATE: creates a resource
* READ: reads a single resource
* SCAN: reads multiple resources
* UPDATE: updates/modifies a resource
* DELETE: deletes a resource

# New abstract operations:
If you want to add new abstact operations:
1. Add the operation to AbstractPatternOperation class (Backend, package patternconfiguration).
2. Implement a matching unit (class implementing the MatchingUnit interface; see Backend, package matching.units). 
If a unit matches it must fill all requires fields based on the openAPI specification (or return null if it does not match). 
You can also copy&paste an already implemented unit and adjust the code to your needs.
3. Open class ResourceMapping (Backend, package mapping) and register your matching unit in the constructor (consider the order).
4. You may need, depending on the use case, also additional linking units.
