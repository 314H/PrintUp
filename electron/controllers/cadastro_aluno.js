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


//máscara para o CPF
$("#input_cadastroCPFAluno").mask("000.000.000-00")


// realizar o cadastro do CPF do aluno no sistema
async function cadastrar() {
    const cpfEntrada = document.getElementById('input_cadastroCPFAluno').value
    const cpfFormatado = retirarCaracteresEspeciais(cpfEntrada)

    const referenceAluno = firebase.database()

    // CPF tem que ter 11 caracteres para ser válido
    if(cpfFormatado.length != 11) {
        alert('CPF inválido!')
        document.getElementById('input_cadastroCPFAluno').focus()
    } else {

        // pesquisar no banco se o usuario já existe
        await referenceAluno.ref(`usuarios/aluno/${cpfFormatado}`).once('value').then(function (snapshot) {
            const verificarCpf = snapshot.val()
    
            // se não estiver ainda cadastrado, cria um objeto aluno para cadastro
            if (verificarCpf == null) {
                let aluno = {
                    nome: '',
                    email: '',
                    cpf: cpfFormatado
                }
    
                // cadastrar aluno no firebase database
                referenceAluno.ref('usuarios/aluno/').child(aluno.cpf).set(aluno)
                    .then(function () {
                        alert(`Cadastro do CPF: ${cpfEntrada} efetuado com sucesso!`)
                        window.location.reload()
                    })
                    .catch(function(error) {
                        alert(`Erro ${error} no cadastro do CPF: ${cpfEntrada}!`)
                        document.getElementById('input_cadastroCPFAluno').focus()
                    })
            } else {
                alert(`O CPF: ${cpfEntrada} já existe no sistema!`)
                document.getElementById('input_cadastroCPFAluno').focus()
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


// preencher tabela com alunos que ainda não fizeram cadastro
(function ($) {
    usuariosSemCadastro = function () {
  
      const referenceAluno = firebase.database()

    // pegar todos os alunos cadastrados no banco
    referenceAluno.ref('usuarios/aluno').once('value').then(function (snapshot) {
    const arrayAlunos = snapshot.val()

    // percorrer todos os alunos cadastrados que vieram do banco
    for (const aluno in arrayAlunos) {

        // se email do aluno for vazio mostra na tabela
        if ((arrayAlunos[aluno].email) == "") {

            var novaLinha = $("<tr>");
            var colunas = "";
            colunas += '<td>sem cadastro</td>'

            colunas += `<td>${arrayAlunos[aluno].cpf}</td>`

            novaLinha.append(colunas);

            $('#tabela_alunoSemcadastro').append(novaLinha)
            
        }
    }
    })
    return false
}
})(jQuery)