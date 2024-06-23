# Crypto

Este programa permite esconder y revelar mensajes dentro de imagenes `BMP` usando los siguientes algoritmos de esteganografiado:

- LSB de 1 bit
- LSB de 4 bits
- [LSB Improved (MOHAMMED ABDUL MAJEED, ROSSILAWATI SULAIMAN)](https://www.jatit.org/volumes/Vol80No2/16Vol80No2.pdf)

Tambien permite encriptar los mensajes a ocultar con los siguientes algoritmos de cifrado:
- AES128
- AES192
- AES256
- 3DES

Y los siguientes modos de cifrado en bloque:
- ECB
- CFB8
- OFB
- CBC



## Instalacion


Clonamos el repositorio.

```sh
git clone git@github.com:/ImNotGone/Crypto
```

Luego compilamos el programa

```sh
mvn package appassembler:assemble
```

En `target/package/bin` se encuentra un script que ejecuta el programa. Por lo tanto entramos en el siguiente directorio.

```
cd target/package/bin
```

Listo! Ya podemos ejecutar el programa.

```
./stegobmp -h
```

## Ejecucion

### Ocultar

Para ocultar un mensaje se debe correr lo siguiente:

```sh
stegobmp -embed -in <input file> -p <cover file> -out <output file> -steg <LSB1|LSB4|LSBI> [-pass <password>] [-a <aes128|aes192|aes256|des>] [-m <ecb|cfb|ofb|cbc>]
```
- **embed**: Indica el modo de operación para ocultar un mensaje en una imagen.
- **in**: Archivo de entrada que contiene el mensaje a ocultar (obligatorio).
- **p**: Archivo de imagen que servirá como cover para ocultar el mensaje (obligatorio).
- **out**: Archivo de salida que contendrá la imagen con el mensaje oculto (obligatorio).
- **steg**: Método de esteganografía a utilizar (obligatorio). Puede ser:
  - _LSB1_: Utiliza el último bit de cada byte de la imagen.
  - _LSB4_: Utiliza los últimos 4 bits de cada byte de la imagen.
  - _LSBI_: Utiliza la [técnica mejorada de LSB](https://www.jatit.org/volumes/Vol80No2/16Vol80No2.pdf).
- **pass**: Contraseña para cifrar el mensaje (opcional).
- **a**: Algoritmo de cifrado (opcional). Puede ser:
  - _aes128_ (por defecto)
  - _aes192_
  - _aes256_
  - _des_
- **m**: Modo de operación del algoritmo de cifrado (opcional). Puede ser:
  - _ecb_
  - _cfb_
  - _ofb_
  - _cbc_ (por defecto)

### Extraer

Para extraer un mensaje se debe correr lo siguiente:

```sh
stegobmp -extract -p <cover file> -out <output file> -steg <LSB1|LSB4|LSBI> [-pass <password>] [-a <aes128|aes192|aes256|des>] [-m <ecb|cfb|ofb|cbc>]
```

- **extract**: Indica el modo de operación para extraer un mensaje de una imagen.
- **p**: Archivo de imagen que contiene el mensaje oculto (obligatorio).
- **out**: Archivo de salida que contendrá el mensaje extraído (obligatorio).
- **steg**: Método de esteganografía a utilizar (obligatorio). Puede ser:
  - _LSB1_: Utiliza el último bit de cada byte de la imagen.
  - _LSB4_: Utiliza los últimos 4 bits de cada byte de la imagen.
  - _LSBI_: Utiliza la [técnica mejorada de LSB](https://www.jatit.org/volumes/Vol80No2/16Vol80No2.pdf).
- **pass**: Contraseña para descifrar el mensaje (opcional).
- **a**: Algoritmo de cifrado (opcional). Puede ser:
  - _aes128_ (por defecto)
  - _aes192_
  - _aes256_
  - _des_
- **m**: Modo de operación del algoritmo de cifrado (opcional). Puede ser:
  - _ecb_
  - _cfb_
  - _ofb_
  - _cbc_ (por defecto)

