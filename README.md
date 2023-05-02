# FileSharing ETL Services

FileSharing ETL Services é um conjunto de micro serviços para coleta de arquivos (Extract), processamento (Transform) e Carga no Banco (Load) para diversos processos da Oi.

## Dependências

Netbeans 12, Java 11, Maven 3.6

## Construção

Utilize o docker no servidor X para construção da imagem e instalação no docker registry X.

```bash
mvn -Dfile.encoding=UTF-8 -Pk8s,k8s_hml clean install
```

## Licença
@ Copyright 2021 everis group
