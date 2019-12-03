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
$("#input_editarCPFProfessor").mask("000.000.000-00");


// carregar dados para preencher os inputs
async function carregarDados() {
    const inputNome = document.getElementById('input_editarNomeProfessor')
    const inputEmail = document.getElementById('input_editarEmailProfessor')
    const inputCPF = document.getElementById('input_editarCPFProfessor')

    // variável para pegar id que vem da pagina anterior
    var link = window.location.href
    var resultado = link.substring(link.indexOf("?") + 4)

    const referenciaProfessor = firebase.database()


    // pesquisar no firebase database pelo id que veio da tela anterior
    await referenciaProfessor.ref(`usuarios/professor/${resultado}`).once('value').then(function (snapshot) {
        const professor = snapshot.val()


        // setar nome e email do professor pesquisado para o input
        inputNome.value = professor.nome
        inputEmail.value = professor.email

        // setar CPF formatado do aluno pesquisado para o input
        const cpfSeparado = professor.cpf.split("")

        // formatar o CPF
        const cpfFormatado = cpfSeparado[0]+cpfSeparado[1]+cpfSeparado[2]+'.'+cpfSeparado[3]+cpfSeparado[4]+cpfSeparado[5]+'.'+cpfSeparado[6]+cpfSeparado[7]+cpfSeparado[8]+'-'+cpfSeparado[9]+cpfSeparado[10]

        inputCPF.value = cpfFormatado

        // variaveis globais
        nomeGlobal = professor.nome
        emailGlobal = professor.email
        cpfGlobal = professor.cpf
        
    })
    $('#button_editarProfessor').removeAttr('disabled')
}


// editar usuario firebase
function editar() {
    const inputNome = document.getElementById('input_editarNomeProfessor').value
    const inputEmail = document.getElementById('input_editarEmailProfessor').value
    const inputCPF = document.getElementById('input_editarCPFProfessor').value
    const cpfFormatado = retirarCaracteresEspeciais(inputCPF)

    // transformar nome em minusculo para pesquisas posteriores
    const nomeMinusculo = inputNome.toLowerCase()


    // referencia banco de dados
    var referenciaProfessor = firebase.database().ref(`usuarios/professor/`)

    let professor = {
        "nome": inputNome,
        "email": inputEmail,
        "cpf": cpfFormatado,
        "nomeLowerCase": nomeMinusculo
    }
    
    if((inputNome.trim() == '') || (inputEmail.trim() == '') || (inputCPF.trim() == '')) {
        alert('Preencha todos os campos corretamente!')
    } else {
        if((inputEmail.indexOf("@") != -1)  && (inputEmail.indexOf(".com") != -1)) {
            
            // verificar se CPF é válido
            if(cpfFormatado.length != 11) {
                alert('CPF inválido!')
            } else {

            // remover antigo usuario
            referenciaProfessor.child(cpfGlobal).remove()
            .then(function() {

                // adicionar usuario atualizado
                referenciaProfessor.child(professor.cpf).set(professor)
                    .then(function() {

                        // referencia para firebase auth
                        const autenticacao = firebase.auth()

                        // autenticar no firebase auth com antigo cpf e email
                        autenticacao.signInWithEmailAndPassword(emailGlobal, cpfGlobal)
                        .then(function(userCredential) {

                            // pegar credencial de autenticação
                            const user = userCredential.user

                            // atualizar email de autenticação do usuario
                            user.updateEmail(inputEmail)
                                .then(function() {
                                        
                                    // atualizar senha de autenticação do usuario
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
        } else {
            alert('Formato de email inválido!')
        }
    }
}



//retirar caracteres especiais do CPF
function retirarCaracteresEspeciais(cpfEntrada) {
    const cpf01 = cpfEntrada.replace('.', '')
    const cpf02 = cpf01.replace('.', '')
    const cpfSaida = cpf02.replace('-', '')
    return cpfSaida
}