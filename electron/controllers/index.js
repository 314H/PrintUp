const path = require('path')
const shell = require('electron').shell
const print = require('print-js')
const { BrowserWindow } = require('electron').remote
const fs = require('fs')
const http = require('https')
var admin = require("firebase-admin")
var serviceAccount = require("./token/token.json")


// configuração do firebase e inicialização do firebase
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
      for (const aluno in arrayAlunos) {

        var novaLinha = $(`<tr id=${aluno}>`);
        var colunas = "";
        colunas += '<td>' + arrayAlunos[aluno].nome_usuario + '</td>';

        colunas += '<td>' + arrayAlunos[aluno].numero_copias + '</td>';

        colunas += '<td><input type="button" id="' + aluno +
          '" class="btn btn-mini btn-default buttonLista" value="imprimir"  onclick="imprimir(\'' + arrayAlunos[aluno].url + '\')"/>'+
          '<input type="button" id="' + aluno +
          '" class="btn btn-mini btn-positive" value="notificação" onClick="arquivoImpressoAluno(id)"/>' +
          ' <input type="button" id="' + aluno +
          '" class="btn btn-mini btn-negative" value="apagar" onClick="arquivoEntregueAluno(id)"/></td>';

        colunas += '<td>'+arrayAlunos[aluno].status+'</td>'
        novaLinha.append(colunas);
        $('#tabelaDeAlunos').append(novaLinha);
      }
    })
    return false;
  };
})(jQuery);


// preencher lista html com lista de impressao dos professores do firebase
(function ($) {
  preencherlistaProfessores = function () {

    const referenceAluno = firebase.database();
    referenceAluno.ref('imprimir/professor').once('value').then(function (snapshot) {

      const arrayProfessores = snapshot.val();
      for (const professor in arrayProfessores) {

        var novaLinha = $(`<tr id=${professor}>`);
        var colunas = "";

        colunas += '<td>' + arrayProfessores[professor].nome_usuario + '</td>';

        colunas += '<td>' + arrayProfessores[professor].numero_copias + '</td>';

        colunas += '<td><input type="button" id="' + professor +
          '" class="btn btn-mini btn-default buttonLista" value="imprimir" onclick="imprimir(\'' + arrayProfessores[professor].url + '\')"/>'+
          '<input type="button" id="' + professor +
          '" class="btn btn-mini btn-positive" value="notificação" onClick="arquivoImpressoProfessor(id)"/>' +
          ' <input type="button" id="' + professor +
          '" class="btn btn-mini btn-negative" value="apagar" onClick="arquivoEntregueProfessor(id)"/></td>';

        colunas += '<td>'+arrayProfessores[professor].status+'</td>'

        novaLinha.append(colunas);
        $('#tabelaDeProfessores').append(novaLinha);

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

  // pegar token de identificação para o dispostivo do usuario
  referenceAluno.ref(`imprimir/aluno/${id}/nome_usuario`).once('value').then(function (snapshot) {
    const nome = snapshot.val()

    referenceAluno.ref(`usuarios/aluno`).orderByChild("nome").equalTo(nome).on('child_added', function(snapshot) {

      referenceAluno.ref(`usuarios/aluno/${snapshot.key}`).once("value").then(function(snapshot) {
        const dados = snapshot.val()

        var registrationToken = dados.tokenNotification

        var payload = {
          notification: {
            title: "PrintUp",
            body: "Seu arquivo já foi impresso, pode pegá-lo no xerox!"
          }
        }
        
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

  // pegar token de identificação para o dispostivo do usuario
  referenceProfessor.ref(`imprimir/professor/${id}/nome_usuario`).once('value').then(function (snapshot) {
    const nome = snapshot.val()

    referenceProfessor.ref(`usuarios/professor`).orderByChild("nome").equalTo(nome).on('child_added', function(snapshot) {

      referenceProfessor.ref(`usuarios/professor/${snapshot.key}`).once("value").then(function(snapshot) {
        const dados = snapshot.val()

        var registrationToken = dados.tokenNotification

        var payload = {
          notification: {
            title: "PrintUp",
            body: "Seu arquivo já foi impresso, pode pegá-lo no xerox!"
          }
        }
        
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


// apagar pedido de impressão e arquivo do storage
async function arquivoEntregueAluno(id) {
  const banco_dados = firebase.database()
  const storage = firebase.storage()
  await banco_dados.ref(`imprimir/aluno/${id}`).once('value').then(function (snapshot) {
    const dados = snapshot.val()
    const url = dados.url
    const sep1 = url.split("aluno%2F").pop()
    var nome_arquivo = sep1.split("?alt").shift()

    banco_dados.ref(`imprimir/aluno/${id}`).remove()
    .then(function() {
      storage.ref(`imprimir/aluno/${nome_arquivo}`).delete()
      .then(function () {
        window.location.reload()
      })
      .catch(function (error) {
        window.location.reload()
      })
    })
    .catch(function(error) {
      alert("Erro ao apagar arquivo!")
    })    
  })
}


// apagar pedido de impressão e arquivo do storage
async function arquivoEntregueProfessor(id) {
  const banco_dados = firebase.database()
  const storage = firebase.storage()
  await banco_dados.ref(`imprimir/professor/${id}`).once('value').then(function (snapshot) {
    const dados = snapshot.val()
    const url = dados.url
    const sep1 = url.split("professor%2F").pop()
    var nome_arquivo = sep1.split("?alt").shift()

    banco_dados.ref(`imprimir/professor/${id}`).remove()
    .then(function() {
      storage.ref(`imprimir/professor/${nome_arquivo}`).delete()
      .then(function () {
        window.location.reload()
      })
      .catch(function (error) {
        alert("Erro ao apagar arquivo!")
      })
    })
    .catch(function(error) {
      alert("Erro ao apagar arquivo!")
    })    
  })
}


// abrir arquivo selecionado
async function imprimir(nome_arquivo) {
  extensao = pegarExtensao(nome_arquivo)
  
  //Salvar o arquivo no disco e abrir com openExternal

  //Fazer download do arquivo usando o modulo request (npm install requests --save)

  // Ultima coisa: Detectar o formato do arquivo para salvar no sistema de arquivos com a extensão correta.

  const filePath = `meuarquivo${extensao}`
  const file = fs.createWriteStream(filePath);
  const request = await http.get(nome_arquivo, function(response) {
    response.pipe(file);

    shell.openExternal(filePath)
  })
  window.location.reload()
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