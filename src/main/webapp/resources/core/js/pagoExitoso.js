
    document.addEventListener('DOMContentLoaded', () => {
    const { jsPDF } = window.jspdf;

    document.getElementById('btn-imprimir').addEventListener('click', async function (e) {
    e.preventDefault();

    const contenido = document.getElementById('contenido-imprimible');
    if (!contenido) {
    console.error('No se encontró el contenido a imprimir');
    return;
}

    this.style.display = 'none';
    await new Promise(resolve => setTimeout(resolve, 50));

    const canvas = await html2canvas(contenido, {
    scale: 1,
    useCORS: true,
    width: contenido.scrollWidth,
    height: contenido.scrollHeight
});

    this.style.display = 'inline-block';

    const imgData = canvas.toDataURL('image/jpeg', 0.7);
    const pdf = new jsPDF('p', 'px', [contenido.scrollWidth, contenido.scrollHeight]);

    const imgProps = pdf.getImageProperties(imgData);
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = (imgProps.height * pdfWidth) / imgProps.width;

    pdf.addImage(imgData, 'JPEG', 0, 0, pdfWidth, pdfHeight);

        const pdfBlob = pdf.output('blob');
        const pdfUrl = URL.createObjectURL(pdfBlob);

        window.open(pdfUrl, '_blank');
    // Guardar el PDF directamente
    // pdf.save('comprobante.pdf');
});
});

