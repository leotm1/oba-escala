# Oba Escala

Escala mensal de folgas da loja. Funciona no celular e no computador, também sem internet.

## Publicar no GitHub Pages (uma vez)

1. Entre em github.com com a sua conta e toque em **New repository**.
2. Nome: `oba-escala`. Deixe como **Public** e toque em **Create repository**.
3. Toque em **uploading an existing file** e envie todos os arquivos desta pasta (inclusive `sw.js`, `manifest.webmanifest`, os ícones e `jspdf.umd.min.js`). Toque em **Commit changes**.
4. Abra **Settings > Pages**. Em **Branch**, escolha `main` e a pasta `/ (root)` e toque em **Save**.
5. Em um ou dois minutos o endereço fica pronto: `https://SEU-USUARIO.github.io/oba-escala/`.

## Instalar no celular

- **Android (Chrome):** abra o endereço, toque nos três pontos e em **Instalar app** ou **Adicionar à tela inicial**.
- **iPhone (Safari):** abra o endereço, toque em **Compartilhar** e em **Adicionar à Tela de Início**.

Depois de aberto uma vez, o app funciona sem internet.

## Onde ficam os dados

Os dados ficam guardados no próprio aparelho. Celular e computador não se sincronizam sozinhos.
Para levar os dados de um aparelho para outro: **Loja e feriados > Baixar backup** em um, e **Restaurar backup** no outro.
Faça um backup de vez em quando: se o navegador for limpo ou o app desinstalado, os dados do aparelho são apagados.

## Atualizar o app

Envie o novo `index.html` para o repositório e aumente o número em `sw.js` (`oba-escala-v1` para `oba-escala-v2`).
Os dados guardados no aparelho continuam.
