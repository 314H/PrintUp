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
$("#input_pesquisaCPFAluno").mask("000.000.000-00");


// pesquisa do aluno no firebase database
async function pesquisar() {

    // limpar tabela de resultados
    $('#tabela_pesquisaAluno tbody').remove()

    const inputNome = document.getElementById('input_pesquisaNomeAluno').value
    const inputCPF = document.getElementById('input_pesquisaCPFAluno').value
    const cpfFormatado = retirarCaracteresEspeciais(inputCPF)

    // transformar nome em minusculo
    const nomeMinusculo = inputNome.toLowerCase()

    // criar referência para o firebase
    const referencia = firebase.database()
    const referenciaAluno = referencia.ref("usuarios/aluno/")

    if (cpfFormatado == '') {

        // pesquisa aluno por nome
        referenciaAluno.orderByChild('nomeLowerCase').startAt(nomeMinusculo).endAt(`${nomeMinusculo}\uf8ff`).on('child_added', function(snapshot) {
            
            // pegar dados do aluno usando a key dos aluno entrado
            referencia.ref(`usuarios/aluno/${snapshot.key}`).once('value').then(function(snapshot) {
                const resultado = snapshot.val()

                // colocar na tabela os resultados encontrados
                var novaLinha = $("<tbody><tr>")

                var colunas = ""

                // se usuario já tiver cadastro aparece seus dados, senão aparece 'indefinido' (if ternário)
                colunas += `<td>${((resultado.nome == '')?'nome indefinido':resultado.nome)}</td>`

                colunas += `<td>${((resultado.email == '')?'email indefinido':resultado.email)}</td>`

                colunas += `<td>${resultado.cpf}</td>`

                if(resultado.email == '') {
                    colunas += "<td>cadastro necessário</ td>"
                } else {
                    colunas += `<td><input type="button" id="${snapshot.key}" class="btn btn-primary" value="Editar" onClick="editar(id)" /></td>`
                }
                
                colunas += `<td><input type="button" id="${snapshot.key}" class="btn btn-positive" value="Apagar" onClick="deletar(id, '${resultado.email}')" /></td>`

                colunas += "</tr>"

                colunas += "</tbody>"

                novaLinha.append(colunas)

                $('#tabela_pesquisaAluno').append(novaLinha)
            })
        })
    } else if (inputNome == '') {
        
        // pesquisa de aluno por CPF
        referenciaAluno.orderByChild("cpf").startAt(cpfFormatado).endAt(`${cpfFormatado}\uf8ff`).on('child_added', function(snapshot) {
            
            // pegar dados do aluno usando a key dos aluno entrado
            referencia.ref(`usuarios/aluno/${snapshot.key}`).once('value').then(function(snapshot) {
                const resultado = snapshot.val()
        
                // colocar na tabela os resultados encontrados
                var novaLinha = $("<tbody><tr>")

                var colunas = ""

                // se usuario já tiver cadastro aparece seus dados, senão aparece 'indefinido' (if ternário)
                colunas += `<td>${((resultado.nome == '')?'nome indefinido':resultado.nome)}</td>`

                colunas += `<td>${((resultado.email == '')?'email indefinido':resultado.email)}</td>`

                colunas += `<td>${resultado.cpf}</td>`

                if(resultado.email == '') {
                    colunas += "<td>cadastro necessário</ td>"
                } else {
                    colunas += `<td><input type="button" id="${snapshot.key}" class="btn btn-primary" value="Editar" onClick="editar(id)" /></td>`
                }

                colunas += `<td><input type="button" id="${snapshot.key}" class="btn btn-positive" value="Apagar" onClick="deletar(id, '${resultado.email}')" /></td>`

                colunas += "</tr>"

                colunas += "</tbody>"
        
                novaLinha.append(colunas);

                $('#tabela_pesquisaAluno').append(novaLinha);
            })
        })
    } else if ((cpfFormatado != '') && (inputNome != '')) {

        // pesquisa de aluno por CPF
        referenciaAluno.orderByChild("cpf").startAt(cpfFormatado).endAt(`${cpfFormatado}\uf8ff`).on('child_added', function(snapshot) {
            
            // pegar dados do aluno usando a key dos aluno entrado
            referencia.ref(`usuarios/aluno/${snapshot.key}`).once('value').then(function(snapshot) {
                const resultado = snapshot.val()
        
                // colocar na tabela os resultados encontrados
                var novaLinha = $("<tbody><tr>")

                var colunas = ""

                // se usuario já tiver cadastro aparece seus dados, senão aparece 'indefinido' (if ternário)
                colunas += `<td>${((resultado.nome == '')?'nome indefinido':resultado.nome)}</td>`

                colunas += `<td>${((resultado.email == '')?'email indefinido':resultado.email)}</td>`

                colunas += `<td>${resultado.cpf}</td>`

                if(resultado.email == '') {
                    colunas += "<td>cadastro necessário</ td>"
                } else {
                    colunas += `<td><input type="button" id="${snapshot.key}" class="btn btn-primary" value="Editar" onClick="editar(id)" /></td>`
                }
                
                colunas += `<td><input type="button" id="${snapshot.key}" class="btn btn-positive" value="Apagar" onClick="deletar(id, '${resultado.email}')" /></td>`

                colunas += "</tr>"

                colunas += "</tbody>"
        
                novaLinha.append(colunas);

                $('#tabela_pesquisaAluno').append(novaLinha);
            })
        })
    } else if ((cpfFormatado == '') && (inputNome == '')) {

        // mensagem mostrada se nenhum parâmetro for adicionado
        alert('Nenhum parâmetro de pesquisa foi adicionado!')
    }
}


//retirar caracteres especiais do CPF
function retirarCaracteresEspeciais(cpfEntrada) {
    const cpf01 = cpfEntrada.replace('.', '')
    const cpf02 = cpf01.replace('.', '')
    const cpfSaida = cpf02.replace('-', '')
    return cpfSaida
}


// deletar usuario do firebase
function deletar(cpf, email) {

    // referencia para o firebase database
    const referenciaAluno = firebase.database()

    referenciaAluno.ref(`usuarios/aluno/${cpf}`).remove()
        .then(function () {
            
            // referencia para firebase auth
            const autenticacao = firebase.auth()

            if(email != "") {
                autenticacao.signInWithEmailAndPassword(email, cpf)
            .then(function(userCredential) {

                // pegar credencial de autenticação
                const user = userCredential.user

                // deletar autenticação do usuario
                user.delete()
                .then(function() {
                    alert('Usuário apagado com sucesso!')
                    window.location.reload()
                })
                .catch(function(error) {
                    alert('Erro ao apagar o usuário!')
                    window.location.reload()
                })
                
            })
            .catch(function(error) {
                alert('Erro ao apagar o usuário!')
                window.location.reload()
            })
            } else {
                alert('Usuário apagado com sucesso!')
                window.location.reload()
            }
        })
        .catch(function (error) {
            alert('Erro ao apagar o usuário!')
            window.location.reload()
        })
}


// abrir janela para edição de usuario
function editar(id) {
    window.open('./editar_aluno.html?id=' + id, 'Editar aluno')
    return false
}