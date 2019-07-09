import jsf from 'json-schema-faker';

function fakeSchema(schema) {
    print(jsf.generate(schema));
}
exports=fakeSchema;