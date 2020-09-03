# Implemented linking units:
* BindingLinker: Links request based on service links
* ParameterNameLinker: Links requests based on parameter names (1:1-match)
* IdLinker: Links requests based on the substring "id"

# New linking units:
If you want to add new linking units:
1. Implement a linking unit (class implementing the Linker interface; see Worker, package linking.linkerunits). 
If a unit links, it must fill all required fields in the currentRequest based on the openAPI specification and
the dependent request; and must return the updated current request (or return null if there is no link). 
You can also copy&paste an already implemented unit and adjust the code to your needs.
2. Open class LinkController (Worker, package linking) and register your linking unit in the constructor (consider the order).
3. Test it!
