# openISBT

OpenISBT is an Intelligent Service Benchmark Tool to benchmark microservice-based applications based on their OpenAPI3.0 interface description files.  
[Benchmarking Microservice Performance: A Pattern-based Approach](https://dbermbach.github.io/publications/2020-sac-dads-microservices.pdf) 

If you use this software in a publication, please cite it as:

### Text
Martin Grambow, Lukas Meusel, Erik Wittern, David Bermbach. **Benchmarking Microservice Performance: A Pattern-based Approach**. In: Proceedings of the 35th ACM Symposium on Applied Computing (SAC 2020). ACM 2020.

### BibTeX
```TeX
@inproceedings{paper_grambow_benchmarking_microservices,
	title = {{Benchmarking Microservice Performance: A Pattern-based Approach}},
	booktitle = {Proceedings of the 35th ACM Symposium on Applied Computing (SAC 2020)},
	publisher = {ACM},
	author = {Martin Grambow and Lukas Meusel and Erik Wittern and David Bermbach},
	year = {2020}
}
```

A full list of our [publications](https://www.mcc.tu-berlin.de/menue/forschung/publikationen/parameter/en/) and [prototypes](https://www.mcc.tu-berlin.de/menue/forschung/prototypes/parameter/en/) is available on our group website.

## Releases
* version 0.1 (d1b6c4f) was used in publication "Benchmarking Microservice Performance: A Pattern-based Approach"
* version 0.2 (fd9e506) introduces a batch benchmark via terminal
* version 0.3 (a10737b) application-wide benchmarking using manual service links
* version 0.4 (tbd) Clean up deprecated components, better documentation

## License

The code in this repository is licensed under the terms of the [MIT](./LICENSE) license.

# Usage
* [Benchmark](openISBTBatch.md)

## Futher Information (for developers)
* [Abstract operations and matching units](abstractOperations.md)
* [Linking(units)](linkingUnits.md)
