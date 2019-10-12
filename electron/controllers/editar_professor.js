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
$("#inputEditarCPFProfessor").mask("000.000.000-00");


// carregar dados para preencher os inputs
async function carregarDados() {
    const inputNome = document.getElementById('inputEditarNomeProfessor')
    const inputEmail = document.getElementById('inputEditarEmailProfessor')
    const inputCPF = document.getElementById('inputEditarCPFProfessor')

    // pegar id que vem da paǵina anterior
    var link = window.location.href
    var resultado = link.substring(link.indexOf("?") + 4)

    const banco_dados = firebase.database()

    await banco_dados.ref(`usuarios/professor/${resultado}`).once('value').then(function (snapshot) {
        const professor = snapshot.val()
        inputNome.value = professor.nome
        inputEmail.value = professor.email

        const cpfSeparado = professor.cpf.split("")
        const cpfFormatado = cpfSeparado[0]+cpfSeparado[1]+cpfSeparado[2]+'.'+cpfSeparado[3]+cpfSeparado[4]+cpfSeparado[5]+'.'+cpfSeparado[6]+cpfSeparado[7]+cpfSeparado[8]+'-'+cpfSeparado[9]+cpfSeparado[10]

        inputCPF.value = cpfFormatado

        // variaveis globais
        nomeGlobal = professor.nome
        emailGlobal = professor.email
        cpfGlobal = professor.cpf
        
    })
    $('#buttonEditarProfessor').removeAttr('disabled')
}


// editar usuario firebase
function editar() {
    const inputNome = document.getElementById('inputEditarNomeProfessor').value
    const inputEmail = document.getElementById('inputEditarEmailProfessor').value
    const inputCPF = document.getElementById('inputEditarCPFProfessor').value
    const cpfFormatado = retirarCaracteresEspeciais(inputCPF)
    const nomeMinusculo = inputNome.toLowerCase()

    // referencia banco de dados
    var banco_dados = firebase.database().ref(`usuarios/professor/`)

    let professor = {
        "nome": inputNome,
        "email": inputEmail,
        "cpf": cpfFormatado,
        "nomeLowerCase": nomeMinusculo
    }
    
    banco_dados.child(cpfGlobal).remove()
        .then(function() {
            banco_dados.child(professor.cpf).set(professor)
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