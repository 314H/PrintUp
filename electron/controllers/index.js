// importações para a página
const shell = require("electron").shell
const fs = require('fs')
const http = require('https')
var admin = require("firebase-admin")
var serviceAccount = require("./token/token.json")


// configuração e inicialização do firebase
async function carregarFirebase() {
  const firebaseConfig = {
    apiKey: "AIzaSyBAPrm6tJa59ZRLFGYQz8WHl0OGjYvlmGk",
    authDomain: "tcc-marcos-willian.firebaseapp.com",
    databaseURL: "https://tcc-marcos-willian.firebaseio.com",
    projectId: "tcc-marcos-willian",
    storageBucket: "tcc-marcos-willian.appspot.com",
    messagingSenderId: "322836423260",
    appId: "1:322836423260:web:d52dbe874173e8c1"
  };

  await firebase.initializeApp(firebaseConfig)
}


// preencher lista html com lista de impressao dos alunos do firebase
(function ($) {
  preencherlistaAlunos = function () {

    const referenceAluno = firebase.database();
    referenceAluno.ref('imprimir/aluno').once('value').then(function (snapshot) {

      const arrayAlunos = snapshot.val();

      // percorrer cada impressão do firebase e criar uma linha
      for (const aluno in arrayAlunos) {

        var novaLinha = $("<tr>");
        var colunas = ""

        colunas += `<td>${arrayAlunos[aluno].nomeUsuario}</td>`

        colunas += `<td>${arrayAlunos[aluno].numeroCopias}</td>`

        colunas += "<td>"

        colunas += `<input type="button" id="${aluno}" class="btn btn-mini btn-default buttonLista" value="imprimir"  onclick="imprimir('${arrayAlunos[aluno].url}')"/>`
          
        colunas += `<input type="button" id="${aluno}" class="btn btn-mini btn-positive" value="notificação" onClick="arquivoImpressoAluno(id)"/>`
          
        colunas += `<input type="button" id="${aluno}" class="btn btn-mini btn-negative buttonLista" value="apagar" onClick="arquivoEntregueAluno(id)"/>`
        
        colunas += "</td>"

        colunas += `<td>${arrayAlunos[aluno].status}</td>`

        novaLinha.append(colunas);

        $('#tabela_impressoesAlunos').append(novaLinha);
      }
    })
    return false;
  };
})(jQuery);


// preencher lista html com lista de impressao dos professores do firebase
(function ($) {
  preencherlistaProfessores = function () {

    const referenceProfessor = firebase.database();
    referenceProfessor.ref('imprimir/professor').once('value').then(function (snapshot) {

      const arrayProfessores = snapshot.val();

      // preencher cada impressão do firebase e criar uma linha
      for (const professor in arrayProfessores) {

        var novaLinha = $("<tr>");
        var colunas = "";

        colunas += `<td>${arrayProfessores[professor].nomeUsuario}</td>`

        colunas += `<td>${arrayProfessores[professor].numeroCopias}</td>`

        colunas += "<td>"
        
        colunas += `<input type="button" id="${professor}" class="btn btn-mini btn-default buttonLista" value="imprimir" onclick="imprimir('${arrayProfessores[professor].url}')"/>`          
        colunas += `<input type="button" id="${professor}" class="btn btn-mini btn-positive" value="notificação" onClick="arquivoImpressoProfessor(id)"/>`
          
        colunas += `<input type="button" id="${professor}" class="btn btn-mini btn-negative buttonLista" value="apagar" onClick="arquivoEntregueProfessor(id)"/>`
        
        colunas += "</td>"

        colunas += `<td>${arrayProfessores[professor].status}</td>`

        novaLinha.append(colunas);

        $('#tabela_impressoesProfessores').append(novaLinha);
      }
    })
    return false;
  };
})(jQuery);


// enviar notificação para o aluno
function arquivoImpressoAluno(id) {
  const referenceAluno = firebase.database();

  // inicializar serviço do google cloud messaging
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://tcc-marcos-willian.firebaseio.com"
  })

  // pegar nome do usuario que tem o pedido de impressão igual 'id'
  referenceAluno.ref(`imprimir/aluno/${id}/nomeUsuario`).once('value').then(function (snapshot) {
    const nome = snapshot.val()

    // pesquisar usuário do firebase com nome igual do pedido de impressão e pegar seu CPF
    referenceAluno.ref(`usuarios/aluno`).orderByChild("nome").equalTo(nome).on('child_added', function(snapshot) {
      const cpfAluno = snapshot.key

      // pegar dados do aluno aluno pesquisado
      referenceAluno.ref(`usuarios/aluno/${cpfAluno}`).once("value").then(function(snapshot) {
        const dados = snapshot.val()

        // token usado para enviar notificação para o dispositivo para o usuário
        var registrationToken = dados.tokenNotification

        // dados da notificação
        var payload = {
          notification: {
            title: "PrintUp",
            body: "Seu arquivo já foi impresso, pode pegá-lo no xerox!"
          }
        }
        
        // configurações extras da notificação
        var options = {
          priority: "high",
          timeToLive: 60 * 60 *24
        }
    
        // envio da notificação
        admin.messaging().sendToDevice(registrationToken, payload, options)
          .then(function(response) {
            alert("Notificação enviada com sucesso!")
            referenceAluno.ref(`imprimir/aluno/${id}`).child("status").set("impresso")
            window.location.reload()
          })
          .catch(function(error) {
            alert("Erro ao enviar notificação!")
            window.location.reload()
          });
      })
    })
  })
}


// enviar notificação para o professor
function arquivoImpressoProfessor(id) {
  const referenceProfessor = firebase.database();

  // inicializar serviço do google cloud messaging
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://tcc-marcos-willian.firebaseio.com"
  })

  // pegar nome do usuario que tem o pedido de impressão igual 'id'
  referenceProfessor.ref(`imprimir/professor/${id}/nomeUsuario`).once('value').then(function (snapshot) {
    const nome = snapshot.val()

    // pesquisar usuário do firebase com nome igual do pedido de impressão e pegar seu CPF
    referenceProfessor.ref(`usuarios/professor`).orderByChild("nome").equalTo(nome).on('child_added', function(snapshot) {

      // pegar dados do aluno aluno pesquisado
      referenceProfessor.ref(`usuarios/professor/${snapshot.key}`).once("value").then(function(snapshot) {
        const dados = snapshot.val()

        // token usado para enviar notificação para o dispositivo para o usuário
        var registrationToken = dados.tokenNotification

        // dados da notificação
        var payload = {
          notification: {
            title: "PrintUp",
            body: "Seu arquivo já foi impresso, pode pegá-lo no xerox!"
          }
        }
        
        // configurações extras da notificação
        var options = {
          priority: "high",
          timeToLive: 60 * 60 *24
        }
    
        // envio da notificação
        admin.messaging().sendToDevice(registrationToken, payload, options)
          .then(function(response) {
            alert("Notificação enviada com sucesso!")
            referenceProfessor.ref(`imprimir/professor/${id}`).child("status").set("impresso")
            window.location.reload()
          })
          .catch(function(error) {
            alert("Erro ao enviar notificação!")
            window.location.reload()
          });
      })
    })
  })
}


// apagar pedido de impressão e arquivo do storage firebase
async function arquivoEntregueAluno(id) {
  const referenceAluno = firebase.database()
  const storage = firebase.storage()

  // pegar dados do pedido de impressão
  await referenceAluno.ref(`imprimir/aluno/${id}`).once('value').then(function (snapshot) {
    const dados = snapshot.val()
    const url = dados.url

    // extrair nome do arquivo salvo no storage da url
    const sep1 = url.split("aluno%2F").pop()
    var nome_arquivo = sep1.split("?alt").shift()

    // remover arquivo da lista de impressão firebase database
    referenceAluno.ref(`imprimir/aluno/${id}`).remove()
    .then(function() {

      // remover arquivo do storage do firebase
      storage.ref(`imprimir/aluno/${nome_arquivo}`).delete()
      .then(function () {
        alert("Arquivo apagado com sucesso!")
        window.location.reload()
      })
      .catch(function (error) {
        window.location.reload()
      })
    })
    .catch(function(error) {
      window.location.reload()
    })    
  })
}


// apagar pedido de impressão e arquivo do storage firebase
async function arquivoEntregueProfessor(id) {
  const referenceProfessor = firebase.database()
  const storage = firebase.storage()

  // pegar dados do pedido de impressão
  await referenceProfessor.ref(`imprimir/professor/${id}`).once('value').then(function (snapshot) {
    const dados = snapshot.val()
    const url = dados.url

    // extrair nome do arquivo salvo no storage da url
    const sep1 = url.split("professor%2F").pop()
    var nome_arquivo = sep1.split("?alt").shift()

    // remover arquivo da lista de impressão firebase database
    referenceProfessor.ref(`imprimir/professor/${id}`).remove()
    .then(function() {

      // remover arquivo do storage do firebase
      storage.ref(`imprimir/professor/${nome_arquivo}`).delete()
      .then(function () {
        alert("Arquivo apagado com sucesso!")
        window.location.reload()
      })
      .catch(function (error) {
        alert("Erro ao apagar arquivo!")
        window.location.reload()
      })
    })
    .catch(function(error) {
      alert("Erro ao apagar arquivo!")
    })    
  })
}


// abrir arquivo selecionado
async function imprimir(url) {
  extensao = await pegarExtensao(url)

  //Salvar o arquivo no disco e abrir com openExternal
  //Fazer download do arquivo usando o modulo request (npm install requests --save)
  // Ultima coisa: Detectar o formato do arquivo para salvar no sistema de arquivos com a extensão correta.

  // caminho do arquivo
  const filePath = `meuarquivo${extensao}`

  // criação do arquivo em disco
  const file = fs.createWriteStream(filePath)

  // requisição http para a url do arquivo selecionado
  const request = await http.get(url, function(response) {
    response.pipe(file);

    // abrir arquivo com programa externo
    shell.openExternal(filePath)
  })
}

// função para pegar extensão do arquivo a ser aberto
function pegarExtensao(url) {
  var extensao = ''
  if(url.indexOf('.docx') != -1) {
    extensao = '.docx'
  } else {
    extensao = '.pdf'
  }
  return extensao
}