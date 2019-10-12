// configuração do firebase
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


//máscara para o CPF
$("#cadastroCPFAluno").mask("000.000.000-00")


// realizar o cadastro do CPF do aluno no sistema
async function cadastrar() {
    const cpfEntrada = document.getElementById('cadastroCPFAluno').value
    const cpfFormatado = retirarCaracteresEspeciais(cpfEntrada)

    const banco_dados = firebase.database()

    if(cpfFormatado.length != 11) {
        alert('CPF inválido!')
    } else {
        await banco_dados.ref(`usuarios/aluno/${cpfFormatado}`).once('value').then(function (snapshot) {
            const verificarCpf = snapshot.val()
    
            if (verificarCpf == null) {
                let aluno = {
                    nome: '',
                    email: '',
                    cpf: cpfFormatado
                }
    
                banco_dados.ref('usuarios/aluno/').child(aluno.cpf).set(aluno)
                    .then(function () {
                        alert(`Cadastro do CPF: ${cpfEntrada} efetuado com sucesso!`)
                        window.location.reload()
                    })
                    .catch(function(error) {
                        alert(`Erro ${error} no cadastro do CPF: ${cpfEntrada}!`)
                    })
            } else {
                alert(`O CPF: ${cpfEntrada} já existe no sistema!`)
                document.getElementById('cadastroCPFAluno').focus()
            }
        })
    }
}


//retirar caracteres especiais do CPF
function retirarCaracteresEspeciais(cpfEntrada) {
    const cpf01 = cpfEntrada.replace('.', '')
    const cpf02 = cpf01.replace('.', '')
    const cpfSaida = cpf02.replace('-', '')
    return cpfSaida
}

(function ($) {
    usuariosSemCadastro = function () {
  
      const referenceAluno = firebase.database();
      referenceAluno.ref('usuarios/aluno').once('value').then(function (snapshot) {

        const arrayAlunos = snapshot.val();
        for (const aluno in arrayAlunos) {
  
          if ((arrayAlunos[aluno].nome) == "") {
            var novaLinha = $("<tr>");
            var colunas = "";
            colunas += '<td>sem cadastro</td>';
  
            colunas += '<td>' + arrayAlunos[aluno].cpf + '</td>';
  
        
            novaLinha.append(colunas);
            $('#tabelasemcadastro').append(novaLinha);
          }
        }
      })
      return false;
    };
  })(jQuery);