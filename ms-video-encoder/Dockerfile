FROM openjdk:21-jdk-slim-buster

ENV PATH="$PATH:/bin/bash" \
    BENTO4_BIN="/opt/bento4/bin" \
    PATH="$PATH:/opt/bento4/bin"

# Atualizar repositórios e instalar dependências
RUN apt-get update && apt-get install -y \
    unzip \
    gcc \
    g++ \
    scons \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Baixar e descompactar Bento4
WORKDIR /tmp/bento4
ENV BENTO4_BASE_URL="http://zebulon.bok.net/Bento4/source/" \
    BENTO4_VERSION="1-5-0-615" \
    BENTO4_CHECKSUM="5378dbb374343bc274981d6e2ef93bce0851bda1" \
    BENTO4_TARGET="" \
    BENTO4_PATH="/opt/bento4" \
    BENTO4_TYPE="SRC"

# Copiar o arquivo Bento4 para o contêiner
COPY libs/Bento4-SRC-1-5-0-615.zip /tmp/bento4/

# Continuar com o processo de instalação
RUN sha1sum -b /tmp/bento4/Bento4-SRC-1-5-0-615.zip | grep -o "^$BENTO4_CHECKSUM " && \
    mkdir -p ${BENTO4_PATH} && \
    unzip /tmp/bento4/Bento4-SRC-1-5-0-615.zip -d ${BENTO4_PATH} && \
    rm -rf /tmp/bento4/Bento4-SRC-1-5-0-615.zip && \
    cd ${BENTO4_PATH} && scons -u build_config=Release target=x86_64-unknown-linux && \
    cp -R ${BENTO4_PATH}/Build/Targets/x86_64-unknown-linux/Release ${BENTO4_PATH}/bin && \
    cp -R ${BENTO4_PATH}/Source/Python/utils ${BENTO4_PATH}/utils && \
    cp -a ${BENTO4_PATH}/Source/Python/wrappers/. ${BENTO4_PATH}/bin

WORKDIR /app

# Configurar variáveis de ambiente
ENV PATH="$PATH:/opt/bento4/bin" \
    SPRING_PROFILES_ACTIVE=dev \
    JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

COPY target/ms-video-encoder-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080 5005

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
