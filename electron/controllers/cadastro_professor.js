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
$("#input_cadastroCPFProfessor").mask("000.000.000-00")


// realizar o cadastro do CPF do professor no sistema
async function cadastrar() {
    const cpfEntrada = document.getElementById('input_cadastroCPFProfessor').value
    const cpfFormatado = retirarCaracteresEspeciais(cpfEntrada)

    const referenceProfessor = firebase.database()

    // verificar se CPF tem 11 caracteres
    if(cpfFormatado.length != 11) {
        alert('CPF inválido!')
    } else {

        // pesquisar CPF do professor no firebase database
        await referenceProfessor.ref(`usuarios/professor/${cpfFormatado}`).once('value').then(function (snapshot) {
            const verificarCpf = snapshot.val()
    
            // se ainda não tiver nada cadastrado com esse CPF cria um professor para cadastro
            if (verificarCpf == null) {
                let professor = {
                    nome: '',
                    email: '',
                    cpf: cpfFormatado
                }
                
                // cadastrar professor no firebase database
                referenceProfessor.ref('usuarios/professor/').child(professor.cpf).set(professor)
                    .then(function () {
                        alert(`Cadastro do CPF: ${cpfEntrada} efetuado com sucesso!`)
                        window.location.reload()
                    })
                    .catch(function (error) {
                        alert(`Erro ${error} no cadastro do CPF: ${cpfEntrada}!`)
                    })
            } else {
                alert(`O CPF: ${cpfEntrada} já existe no sistema!`)
                document.getElementById('input_cadastroCPFProfessor').focus()
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


// preencher a tabela com professores que ainda não completaram seu cadastro
(function ($) {
usuariosSemCadastro = function () {

    const referenceProfessor = firebase.database();

    // pegar todos os professores do banco de dados
    referenceProfessor.ref('usuarios/professor').once('value').then(function (snapshot) {
    const arrayProfessores = snapshot.val();

    // percorrer todos os professores que foram encontrados
    for (const professor in arrayProfessores) {

        // o professor que estiver com email vazio é incluido na tabela
        if ((arrayProfessores[professor].email) == "") {
            var novaLinha = $("<tr>")

            var colunas = ""

            colunas += '<td>sem cadastro</td>'

            colunas += `<td>${arrayProfessores[professor].cpf}</td>`

        
            novaLinha.append(colunas)

            $('#tabela_professorSemCadastro').append(novaLinha)
        }
    }
    })
    return false
};
})(jQuery)