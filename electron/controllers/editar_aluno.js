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
$("#inputEditarCPFAluno").mask("000.000.000-00");


// carregar dados para preencher inputs
async function carregarDados() {
    const inputNome = document.getElementById('inputEditarNomeAluno')
    const inputEmail = document.getElementById('inputEditarEmailAluno')
    const inputCPF = document.getElementById('inputEditarCPFAluno')

    // variável para pegar id que vem da página anterior
    var link = window.location.href
    var resultado = link.substring(link.indexOf("?") + 4);

    const banco_dados = firebase.database()

    await banco_dados.ref(`usuarios/aluno/${resultado}`).once('value').then(function (snapshot) {
        const aluno = snapshot.val()
        inputNome.value = aluno.nome
        inputEmail.value = aluno.email

        const cpfSeparado = aluno.cpf.split("")
        const cpfFormatado = cpfSeparado[0]+cpfSeparado[1]+cpfSeparado[2]+'.'+cpfSeparado[3]+cpfSeparado[4]+cpfSeparado[5]+'.'+cpfSeparado[6]+cpfSeparado[7]+cpfSeparado[8]+'-'+cpfSeparado[9]+cpfSeparado[10]

        inputCPF.value = cpfFormatado

        // variaveis globais
        nomeGlobal = aluno.nome
        emailGlobal = aluno.email
        cpfGlobal = aluno.cpf
    })
    $('#buttonEditarAluno').removeAttr('disabled')
}


// editar usuário do firebase
function editar() {
    const inputNome = document.getElementById('inputEditarNomeAluno').value
    const inputEmail = document.getElementById('inputEditarEmailAluno').value
    const inputCPF = document.getElementById('inputEditarCPFAluno').value
    const cpfFormatado = retirarCaracteresEspeciais(inputCPF)
    const nomeMinusculo = inputNome.toLowerCase()

    // referencia banco de dados
    var banco_dados = firebase.database().ref(`usuarios/aluno/`)

    let aluno = {
        "nome": inputNome,
        "email": inputEmail,
        "cpf": cpfFormatado,
        "nomeLowerCase": nomeMinusculo
    }

    banco_dados.child(cpfGlobal).remove()
        .then(function() {
            banco_dados.child(aluno.cpf).set(aluno)
                .then(function() {

                    const autenticacao = firebase.auth()

                    autenticacao.signInWithEmailAndPassword(emailGlobal, cpfGlobal)
                    .then(function(userCredential) {
                        const user = userCredential.user

                        user.updateEmail(inputEmail)
                            .then(function() {
                                    
                                user.updatePassword(cpfFormatado)
                                .then(function() {
                                    alert('Atualizado com sucesso')
                                    window.close()
                                })
                                .catch(function(error) {
                                    alert(`Erro ao atualizar usuário: ${error}`)
                                })

                            })
                            .catch(function(error) {
                                alert(`Erro ao atualizar usuário: ${error}`)
                            })
                    })
                    .catch(function(error) {
                        alert(`Erro ao atualizar usuário: ${error}`)
                    })
                })
                .catch(function(error) {
                    alert(`Erro ao atualizar usuário: ${error}`)
                })
        })
        .catch(function(error) {
            alert(`Erro ao atualizar usuário: ${error}`)
        })
}


//retirar caracteres especiais do CPF
function retirarCaracteresEspeciais(cpfEntrada) {
    const cpf01 = cpfEntrada.replace('.', '')
    const cpf02 = cpf01.replace('.', '')
    const cpfSaida = cpf02.replace('-', '')
    return cpfSaida
}